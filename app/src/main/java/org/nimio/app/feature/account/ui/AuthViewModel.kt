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

class AuthViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onModeChanged(isLoginMode: Boolean) {
        _uiState.update {
            it.copy(
                isLoginMode = isLoginMode,
                errorMessage = null
            )
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, errorMessage = null) }
    }

    fun submit() {
        val current = _uiState.value
        if (!current.canSubmit || current.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (current.isLoginMode) {
                accountRepository.signIn(
                    email = current.email.trim(),
                    password = current.password
                )
            } else {
                accountRepository.register(
                    email = current.email.trim(),
                    password = current.password,
                    username = current.username.trim(),
                    displayName = current.displayName.trim()
                )
            }

            _uiState.update { state ->
                when (result) {
                    is NimioResult.Success -> state.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        password = if (state.isLoginMode) "" else state.password
                    )
                    is NimioResult.Error -> state.copy(
                        isSubmitting = false,
                        errorMessage = result.throwable.message ?: "Something went wrong."
                    )
                }
            }
        }
    }
}

class AuthViewModelFactory(
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(accountRepository = accountRepository) as T
    }
}

