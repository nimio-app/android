package org.nimio.app.feature.social.domain

enum class ConnectionTier(
    val apiValue: String,
    val displayLabel: String,
    val description: String
) {
    ALL(
        apiValue = "ALL",
        displayLabel = "All",
        description = "Can see all your statuses"
    ),
    CIRCLE(
        apiValue = "CIRCLE",
        displayLabel = "Circle",
        description = "Can see Circle-only and public statuses"
    ),
    MUTUAL(
        apiValue = "MUTUAL",
        displayLabel = "Mutual",
        description = "Standard friend connection"
    )
}

