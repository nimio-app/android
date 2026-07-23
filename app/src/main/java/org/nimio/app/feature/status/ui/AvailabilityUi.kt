package org.nimio.app.feature.status.ui

import org.nimio.app.feature.status.domain.Availability
import androidx.compose.ui.graphics.Color

val Availability.cardColor: Color
    get() = when (this) {
        Availability.FREE -> Color(0xFFD6F5E3)
        Availability.FOCUSED -> Color(0xFFE8E4FF)
        Availability.AWAY -> Color(0xFFFFF1CC)
        Availability.WANT_TO_CONNECT -> Color(0xFFFFE1ED)
        Availability.RESTING -> Color(0xFFE8EAF6)
    }

