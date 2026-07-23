package org.nimio.app.feature.status.sync

/**
 * Abstracts WorkManager scheduling so StatusViewModel stays testable without
 * an Android context or WorkManager in unit tests.
 */
interface StatusExpiryScheduler {
    /** Schedule a one-time worker to fire after [delayMillis]. */
    fun schedule(delayMillis: Long)

    /** Cancel any pending expiry work. */
    fun cancel()
}

