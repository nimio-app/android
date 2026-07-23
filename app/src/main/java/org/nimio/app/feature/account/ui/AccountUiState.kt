package org.nimio.app.feature.account.ui

data class AccountUiState(
    val displayName: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

