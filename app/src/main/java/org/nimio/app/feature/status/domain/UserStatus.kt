package org.nimio.app.feature.status.domain

data class UserStatus(
    val availability: Availability = Availability.AVAILABLE,
    val activity: String = "",
    val updatedAtEpochMillis: Long? = null
)

