package org.nimio.app.feature.status.ui

import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.StatusExpiry

data class StatusUiState(
    val selectedAvailability: Availability = Availability.FREE,
    val activityText: String = "",
    val noteText: String = "",
    val selectedExpiry: StatusExpiry = StatusExpiry.NONE,
    val lastUpdatedEpochMillis: Long? = null,
    val expiresAtEpochMillis: Long? = null,
    val isSaving: Boolean = false,
    val justSaved: Boolean = false
)

