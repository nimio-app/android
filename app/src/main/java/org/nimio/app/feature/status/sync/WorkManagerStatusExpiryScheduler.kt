package org.nimio.app.feature.status.sync

import android.content.Context

/**
 * Routes scheduling calls to the real WorkManager-backed [StatusExpiryWorker].
 */
class WorkManagerStatusExpiryScheduler(
    private val context: Context
) : StatusExpiryScheduler {

    override fun schedule(delayMillis: Long) {
        StatusExpiryWorker.schedule(context, delayMillis)
    }

    override fun cancel() {
        StatusExpiryWorker.cancel(context)
    }
}

