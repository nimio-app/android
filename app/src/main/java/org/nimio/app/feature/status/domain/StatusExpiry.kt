package org.nimio.app.feature.status.domain

enum class StatusExpiry(
    val displayLabel: String,
    val durationMillis: Long?
) {
    NONE(displayLabel = "Until I change it", durationMillis = null),
    FIFTEEN_MIN(displayLabel = "15 minutes", durationMillis = 15 * 60 * 1000L),
    THIRTY_MIN(displayLabel = "30 minutes", durationMillis = 30 * 60 * 1000L),
    ONE_HOUR(displayLabel = "1 hour", durationMillis = 60 * 60 * 1000L),
    TWO_HOURS(displayLabel = "2 hours", durationMillis = 2 * 60 * 60 * 1000L),
    END_OF_DAY(displayLabel = "End of day", durationMillis = null) // calculated at save time
}

