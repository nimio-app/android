package org.nimio.app.feature.sync.domain

interface SyncCoordinator {
    suspend fun enqueueStatusSync()
    suspend fun performImmediateSyncIfNeeded()
}

