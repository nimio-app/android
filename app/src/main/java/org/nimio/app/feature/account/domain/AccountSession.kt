package org.nimio.app.feature.account.domain

data class AccountSession(
    val userId: String,
    val displayName: String,
    val isSignedIn: Boolean
)

