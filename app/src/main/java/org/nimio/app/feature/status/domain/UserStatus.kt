package org.nimio.app.feature.status.domain

data class UserStatus(
    val availability: Availability = Availability.FREE,
    val activity: String = "",
    val note: String = "",
    val visibilityTier: VisibilityTier = VisibilityTier.ALL_CONNECTIONS,
    val updatedAtEpochMillis: Long? = null,
    val expiresAtEpochMillis: Long? = null
)

