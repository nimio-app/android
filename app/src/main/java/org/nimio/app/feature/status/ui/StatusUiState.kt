package org.nimio.app.feature.status.ui

import org.nimio.app.feature.status.domain.Availability

data class StatusUiState(
    val selectedAvailability: Availability = Availability.AVAILABLE,
    val activityText: String = "",
    val lastUpdatedEpochMillis: Long? = null,
    val isSaving: Boolean = false
)

