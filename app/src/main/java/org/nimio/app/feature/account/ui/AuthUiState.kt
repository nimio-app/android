package org.nimio.app.feature.account.ui

data class AuthUiState(
    val isLoginMode: Boolean = false,
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() &&
            password.length >= 8 &&
            (isLoginMode || (username.isNotBlank() && displayName.isNotBlank()))
}

