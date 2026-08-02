package org.nimio.app.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nimio.app.core.common.NimioResult
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.LocalProfile
import org.nimio.app.feature.account.domain.LocalProfileRepository

class AccountViewModel(
    private val repository: LocalProfileRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        userId = profile.userId,
                        email = profile.email,
                        emailVerified = profile.emailVerified,
                        username = profile.username,
                        displayName = profile.displayName,
                        bio = profile.bio,
                        avatarUri = profile.avatarUri,
                        isSaving = false,
                        saved = false,
                        errorMessage = null,
                        isSigningOut = false
                    )
                }
            }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, saved = false, errorMessage = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, saved = false, errorMessage = null) }
    }

    fun onBioChanged(value: String) {
        _uiState.update { it.copy(bio = value, saved = false, errorMessage = null) }
    }

    fun onAvatarChanged(avatarUri: String?) {
        _uiState.update { it.copy(avatarUri = avatarUri, saved = false, errorMessage = null) }
    }

    fun uploadAvatar(filePath: String) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = accountRepository.uploadAvatar(filePath)
            when (result) {
                is NimioResult.Success -> {
                    val current = _uiState.value
                    repository.saveProfile(
                        LocalProfile(
                            userId = current.userId,
                            email = current.email,
                            emailVerified = current.emailVerified,
                            username = current.username,
                            displayName = current.displayName.trim(),
                            bio = current.bio.trim(),
                            avatarUri = result.value,
                            onboardingCompleted = true
                        )
                    )
                    _uiState.update { it.copy(isSaving = false, saved = true, errorMessage = null) }
                }
                is NimioResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, saved = false, errorMessage = result.throwable.message) }
                }
            }
        }
    }

    fun deleteAvatar() {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = accountRepository.deleteAvatar()
            when (result) {
                is NimioResult.Success -> {
                    val current = _uiState.value
                    repository.saveProfile(
                        LocalProfile(
                            userId = current.userId,
                            email = current.email,
                            emailVerified = current.emailVerified,
                            username = current.username,
                            displayName = current.displayName.trim(),
                            bio = current.bio.trim(),
                            avatarUri = null,
                            onboardingCompleted = true
                        )
                    )
                    _uiState.update { it.copy(isSaving = false, saved = true, errorMessage = null) }
                }
                is NimioResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, saved = false, errorMessage = result.throwable.message) }
                }
            }
        }
    }

    fun saveProfile() {
        val current = _uiState.value
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = accountRepository.updateProfile(
                username = current.username,
                displayName = current.displayName,
                bio = current.bio
            )) {
                is NimioResult.Success -> {
                    repository.saveProfile(
                        LocalProfile(
                            userId = current.userId,
                            email = current.email,
                            emailVerified = current.emailVerified,
                            username = current.username.trim(),
                            displayName = current.displayName.trim(),
                            bio = current.bio.trim(),
                            avatarUri = current.avatarUri,
                            onboardingCompleted = true
                        )
                    )
                    _uiState.update { it.copy(isSaving = false, saved = true, errorMessage = null) }
                }

                is NimioResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saved = false,
                            errorMessage = result.throwable.message
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        if (_uiState.value.isSigningOut) return

        _uiState.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            accountRepository.signOut()
            _uiState.update { AccountUiState() }
        }
    }
}

class AccountViewModelFactory(
    private val repository: LocalProfileRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccountViewModel(
            repository = repository,
            accountRepository = accountRepository
        ) as T
    }
}

