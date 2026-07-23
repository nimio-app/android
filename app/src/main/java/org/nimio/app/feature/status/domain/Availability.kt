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
    FOCUSED(
        emoji = "🎯",
        displayLabel = "In deep focus",
        hint = "Heads down, back soon"
    ),
    AWAY(
        emoji = "⏰",
        displayLabel = "Back soon",
        hint = "Stepping away for a bit"
    ),
    WANT_TO_CONNECT(
        emoji = "❤️",
        displayLabel = "Want to connect",
        hint = "Feeling social — reach out"
    ),
    RESTING(
        emoji = "🌙",
        displayLabel = "Taking a break",
        hint = "Resting, catch you tomorrow"
    )
}

