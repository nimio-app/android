package org.nimio.app.feature.status.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.StatusExpiry
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.status.domain.UserStatus

class StatusViewModel(
    private val repository: StatusRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeStatus().collect { status ->
                _uiState.update {
                    it.copy(
                        selectedAvailability = status.availability,
                        activityText = status.activity,
                        noteText = status.note,
                        lastUpdatedEpochMillis = status.updatedAtEpochMillis,
                        expiresAtEpochMillis = status.expiresAtEpochMillis,
                        isSaving = false,
                        justSaved = false
                    )
                }
            }
        }
    }

    fun onAvailabilitySelected(availability: Availability) {
        _uiState.update { it.copy(selectedAvailability = availability) }
    }

    fun onActivityChanged(activity: String) {
        _uiState.update { it.copy(activityText = activity) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(noteText = note) }
    }

    fun onExpirySelected(expiry: StatusExpiry) {
        _uiState.update { it.copy(selectedExpiry = expiry) }
    }

    fun saveStatus() {
        val current = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val expiresAt = when (current.selectedExpiry) {
                    StatusExpiry.NONE -> null
                    StatusExpiry.END_OF_DAY -> {
                        val cal = java.util.Calendar.getInstance()
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                        cal.set(java.util.Calendar.MINUTE, 59)
                        cal.set(java.util.Calendar.SECOND, 59)
                        cal.timeInMillis
                    }
                    else -> current.selectedExpiry.durationMillis?.let { now + it }
                }

                repository.saveStatus(
                    UserStatus(
                        availability = current.selectedAvailability,
                        activity = current.activityText.trim(),
                        note = current.noteText.trim(),
                        updatedAtEpochMillis = now,
                        expiresAtEpochMillis = expiresAt
                    )
                )
                _uiState.update { it.copy(justSaved = true) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}

class StatusViewModelFactory(
    private val repository: StatusRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatusViewModel(repository = repository) as T
    }
}




