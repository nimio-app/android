package org.nimio.app.feature.social.domain

data class VisibleStatus(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val availabilityType: String,
    val note: String = "",
    val visibilityTier: String = "ALL_CONNECTIONS",
    val createdAt: String? = null,
    val expiresAt: String? = null
)

