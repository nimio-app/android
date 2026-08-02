package org.nimio.app.feature.account.ui

data class AccountUiState(
    val userId: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
    val isSigningOut: Boolean = false
)

