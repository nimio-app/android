package org.nimio.app.feature.status.ui

import org.nimio.app.feature.status.domain.Availability
import androidx.compose.ui.graphics.Color

val Availability.cardColor: Color
    get() = when (this) {
        Availability.FREE -> Color(0xFFD6F5E3)
        Availability.BUSY -> Color(0xFFFFF1CC)
        Availability.FOCUS -> Color(0xFFE8E4FF)
        Availability.DRIVING -> Color(0xFFE8EAF6)
        Availability.WANT_TO_TALK -> Color(0xFFFFE1ED)
    }

