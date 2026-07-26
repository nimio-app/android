package org.nimio.app.feature.account.ui

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
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.LocalProfileRepository

data class EmailVerificationBannerUiState(
    val email: String = "",
    val emailVerified: Boolean = true,
    val isDismissed: Boolean = false,
    val isResending: Boolean = false,
    val cooldownSecondsRemaining: Int = 0
) {
    val shouldShow: Boolean
        get() = email.isNotBlank() && !emailVerified && !isDismissed
}

class EmailVerificationBannerViewModel(
    private val profileRepository: LocalProfileRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmailVerificationBannerUiState())
    val uiState: StateFlow<EmailVerificationBannerUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        email = profile.email,
                        emailVerified = profile.emailVerified,
                        isDismissed = if (profile.emailVerified) false else it.isDismissed
                    )
                }
            }
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(isDismissed = true) }
    }

    fun resendVerificationEmail() {
        val current = _uiState.value
        if (current.emailVerified || current.isResending || current.cooldownSecondsRemaining > 0) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true) }
            val result = accountRepository.resendVerification(current.email)
            _uiState.update { it.copy(isResending = false) }

            if (result is NimioResult.Success) {
                startCooldown(seconds = 60)
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            val targetMillis = System.currentTimeMillis() + (seconds * 1_000L)
            while (true) {
                val remaining = ((targetMillis - System.currentTimeMillis() + 999L) / 1_000L)
                    .toInt()
                    .coerceAtLeast(0)
                _uiState.update { it.copy(cooldownSecondsRemaining = remaining) }
                if (remaining == 0) break
                delay(250L)
            }
        }
    }
}

class EmailVerificationBannerViewModelFactory(
    private val profileRepository: LocalProfileRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmailVerificationBannerViewModel(
            profileRepository = profileRepository,
            accountRepository = accountRepository
        ) as T
    }
}

