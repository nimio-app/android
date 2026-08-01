package org.nimio.app.feature.status.domain

enum class VisibilityTier(
    val displayLabel: String,
    val description: String
) {
    ALL_CONNECTIONS(
        displayLabel = "All connections",
        description = "Visible to all accepted friends"
    ),
    CIRCLE_ONLY(
        displayLabel = "Circle only",
        description = "Only visible to Circle-tier connections"
    ),
    CUSTOM_LIST(
        displayLabel = "Custom list",
        description = "Visible to selected people only"
    )
}

