package org.nimio.app.feature.account.domain

data class LocalProfile(
    val displayName: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val onboardingCompleted: Boolean = false
)

