package org.nimio.app.feature.status.ui

import androidx.annotation.StringRes
import org.nimio.app.R
import org.nimio.app.feature.status.domain.Availability

@get:StringRes
val Availability.labelResId: Int
    get() = when (this) {
        Availability.AVAILABLE -> R.string.availability_available
        Availability.BUSY -> R.string.availability_busy
        Availability.FOCUSING -> R.string.availability_focusing
        Availability.AWAY -> R.string.availability_away
    }

