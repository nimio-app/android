package org.nimio.app.feature.sync.data

import org.nimio.app.feature.sync.domain.SyncCoordinator

class NoOpSyncCoordinator : SyncCoordinator {
    override suspend fun enqueueStatusSync() = Unit

    override suspend fun performImmediateSyncIfNeeded() = Unit
}

