package org.nimio.app.feature.status.domain

enum class Availability(
    val emoji: String,
    val displayLabel: String,
    val hint: String
) {
    FREE(
        emoji = "🟢",
        displayLabel = "Free to chat",
        hint = "Open and happy to hear from you"
    ),
    BUSY(
        emoji = "🟠",
        displayLabel = "Busy",
        hint = "Occupied, but you can still reach me"
    ),
    FOCUS(
        emoji = "🎯",
        displayLabel = "Focus",
        hint = "Heads down, back soon"
    ),
    DRIVING(
        emoji = "🚗",
        displayLabel = "Driving",
        hint = "On the road right now"
    ),
    WANT_TO_TALK(
        emoji = "❤️",
        displayLabel = "Want to talk",
        hint = "Feeling social — reach out"
    )
}

