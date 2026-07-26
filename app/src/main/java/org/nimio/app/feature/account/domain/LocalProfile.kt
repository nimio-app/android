package org.nimio.app.feature.account.domain

data class LocalProfile(
    val userId: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val onboardingCompleted: Boolean = false
)

