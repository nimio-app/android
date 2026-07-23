package org.nimio.app.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nimio.app.feature.account.domain.LocalProfile
import org.nimio.app.feature.account.domain.LocalProfileRepository

class AccountViewModel(
    private val repository: LocalProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        displayName = profile.displayName,
                        bio = profile.bio,
                        isSaving = false,
                        saved = false
                    )
                }
            }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, saved = false) }
    }

    fun onBioChanged(value: String) {
        _uiState.update { it.copy(bio = value, saved = false) }
    }

    fun saveProfile() {
        val current = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveProfile(
                LocalProfile(
                    displayName = current.displayName.trim(),
                    bio = current.bio.trim(),
                    onboardingCompleted = true
                )
            )
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }
}

class AccountViewModelFactory(
    private val repository: LocalProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccountViewModel(repository = repository) as T
    }
}

