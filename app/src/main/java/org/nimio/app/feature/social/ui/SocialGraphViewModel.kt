package org.nimio.app.feature.social.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nimio.app.core.common.NimioResult
import org.nimio.app.feature.social.domain.ConnectionActionResult
import org.nimio.app.feature.social.domain.ConnectionStatus
import org.nimio.app.feature.social.domain.ConnectionSummary
import org.nimio.app.feature.social.domain.ConnectionTier
import org.nimio.app.feature.social.domain.PendingActionHint
import org.nimio.app.feature.social.domain.SocialGraphRepository
import org.nimio.app.feature.social.domain.UserSearchResult

data class SocialGraphUiState(
    val connections: List<ConnectionSummary> = emptyList(),
    val count: Int = 0,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val requestSearchQuery: String = "",
    val searchResults: List<UserSearchResult> = emptyList(),
    val isSearchingUsers: Boolean = false,
    val searchErrorMessage: String? = null,
    val pendingOutgoingUserIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val relationshipTier: ConnectionTier = ConnectionTier.ALL,
    val statusFilter: ConnectionStatus? = null
) {
    val pendingRequests: List<ConnectionSummary>
        get() = connections.filter { it.status == ConnectionStatus.PENDING }

    val incomingPendingRequests: List<ConnectionSummary>
        get() = pendingRequests.filter { item ->
            item.pendingActionHint == PendingActionHint.INCOMING ||
                (item.pendingActionHint == null && !item.initiatedByMe)
        }

    val outgoingPendingRequests: List<ConnectionSummary>
        get() = pendingRequests.filter { item ->
            item.pendingActionHint == PendingActionHint.OUTGOING ||
                (item.pendingActionHint == null && item.initiatedByMe)
        }

    val acceptedConnections: List<ConnectionSummary>
        get() = connections.filter { it.status == ConnectionStatus.ACCEPTED }

    val filteredConnections: List<ConnectionSummary>
        get() {
            val query = searchQuery.trim().lowercase()
            if (query.isBlank()) return connections
            return connections.filter { item ->
                item.displayName.lowercase().contains(query) ||
                    item.username.lowercase().contains(query) ||
                    item.counterpartUserId.lowercase().contains(query)
            }
        }

    /**
     * Keyed by the OTHER user's id (friendId from our perspective). Returns the existing
     * status so the UI can show "Pending" / "Connected" instead of a Request button.
     */
    val connectionStatusByUserId: Map<String, ConnectionStatus>
        get() = connections.associate { it.counterpartUserId to it.status }
}

class SocialGraphViewModel(
    private val repository: SocialGraphRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SocialGraphUiState())
    val uiState: StateFlow<SocialGraphUiState> = _uiState.asStateFlow()
    private var searchUsersJob: Job? = null

    init {
        viewModelScope.launch {
            repository.observeConnections().collect { connections ->
                _uiState.update {
                    val normalizedKnownIds = connections.map { connection -> connection.counterpartUserId }.toSet()
                    it.copy(
                        connections = connections,
                        count = connections.size,
                        isLoading = false,
                        pendingOutgoingUserIds = it.pendingOutgoingUserIds - normalizedKnownIds
                    )
                }
            }
        }
        refresh()
    }

    fun onRequestSearchQueryChanged(value: String) {
        _uiState.update {
            it.copy(
                requestSearchQuery = value,
                searchErrorMessage = null,
                message = null,
                errorMessage = null
            )
        }
        val query = value.trim()
        if (query.length < 2) {
            searchUsersJob?.cancel()
            _uiState.update { state ->
                state.copy(
                    searchResults = emptyList(),
                    isSearchingUsers = false,
                    searchErrorMessage = null
                )
            }
            return
        }
        searchUsersJob?.cancel()
        searchUsersJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isSearchingUsers = true, searchErrorMessage = null) }
            when (val result = repository.searchUsers(query = query, limit = 20)) {
                is NimioResult.Success -> _uiState.update {
                    it.copy(isSearchingUsers = false, searchResults = result.value)
                }

                is NimioResult.Error -> _uiState.update {
                    it.copy(
                        isSearchingUsers = false,
                        searchResults = emptyList(),
                        searchErrorMessage = result.throwable.message
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun onRelationshipTierChanged(tier: ConnectionTier) {
        _uiState.update { it.copy(relationshipTier = tier, errorMessage = null, message = null) }
    }

    fun onStatusFilterChanged(status: ConnectionStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.refreshConnections(_uiState.value.statusFilter)) {
                is NimioResult.Success -> _uiState.update {
                    it.copy(isLoading = false, connections = result.value, count = result.value.size)
                }
                is NimioResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.throwable.message)
                }
            }
        }
    }

    fun sendRequest(toUserId: String) {
        val current = _uiState.value
        val userId = toUserId.trim()
        if (userId.isBlank()) return
        val existingStatus = current.connectionStatusByUserId[userId]
        if (existingStatus == ConnectionStatus.PENDING ||
            existingStatus == ConnectionStatus.ACCEPTED ||
            current.pendingOutgoingUserIds.contains(userId)
        ) {
            _uiState.update {
                it.copy(message = "Request already sent or connection already exists.", errorMessage = null)
            }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, message = null) }
        viewModelScope.launch {
            when (val result = repository.requestConnection(userId, current.relationshipTier)) {
                is NimioResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        message = result.value.message ?: "Friend request sent",
                        pendingOutgoingUserIds = it.pendingOutgoingUserIds + userId,
                        requestSearchQuery = "",
                        searchResults = emptyList(),
                        isSearchingUsers = false,
                        searchErrorMessage = null
                    )
                }
                is NimioResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.throwable.message)
                }
            }
            refresh()
        }
    }

    fun accept(fromUserId: String) {
        submitAction { repository.acceptConnection(fromUserId) }
    }

    fun reject(fromUserId: String) {
        submitAction { repository.rejectConnection(fromUserId) }
    }

    fun block(userId: String) {
        submitAction { repository.blockUser(userId) }
    }

    fun updateTier(connectionId: String, tier: ConnectionTier) {
        submitAction { repository.updateRelationshipTier(connectionId, tier) }
    }

    fun remove(friendId: String) {
        viewModelScope.launch {
            when (val result = repository.removeConnection(friendId)) {
                is NimioResult.Success -> refresh()
                is NimioResult.Error -> _uiState.update { it.copy(errorMessage = result.throwable.message) }
            }
        }
    }

    private fun submitAction(block: suspend () -> NimioResult<ConnectionActionResult>) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, message = null) }
        viewModelScope.launch {
            when (val result = block()) {
                is NimioResult.Success -> _uiState.update {
                    it.copy(isSubmitting = false, message = result.value.message ?: "Updated")
                }
                is NimioResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.throwable.message)
                }
            }
            refresh()
        }
    }

    override fun onCleared() {
        searchUsersJob?.cancel()
        super.onCleared()
    }
}

class SocialGraphViewModelFactory(
    private val repository: SocialGraphRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SocialGraphViewModel(repository) as T
    }
}


