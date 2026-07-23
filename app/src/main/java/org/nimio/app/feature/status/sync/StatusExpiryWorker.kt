package org.nimio.app.feature.status.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import org.nimio.app.feature.status.data.StatusPreferencesDataSource
import org.nimio.app.feature.status.domain.UserStatus

class StatusExpiryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dataSource = StatusPreferencesDataSource(applicationContext)
        val current = dataSource.observeStatus().first()
        val expiresAt = current.expiresAtEpochMillis

        if (expiresAt != null && System.currentTimeMillis() >= expiresAt) {
            dataSource.saveStatus(UserStatus())
        }

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
