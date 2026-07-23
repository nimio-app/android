package org.nimio.app.feature.status.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class StatusExpiryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // When status expires, clear it by writing an empty default back to DataStore.
        // We use a coroutine-safe approach by delegating to the repository on the calling thread.
        // This is a no-op for now; full impl will use coroutine worker + repository injection.
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "status_expiry"

        fun schedule(context: Context, delayMillis: Long) {
            val request = OneTimeWorkRequestBuilder<StatusExpiryWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

