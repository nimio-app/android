package org.nimio.app.feature.status.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.status.domain.UserStatus
import org.nimio.app.feature.status.domain.VisibilityTier

class DefaultStatusRepository(
    private val dataSource: StatusPreferencesDataSource
) : StatusRepository {
    override fun observeStatus(): Flow<UserStatus> = dataSource.observeStatus()

    override fun observeActiveStatuses(): Flow<Map<VisibilityTier, UserStatus>> {
        return dataSource.observeStatus().map { status ->
            mapOf(status.visibilityTier to status)
        }
    }

    override suspend fun saveStatus(status: UserStatus) {
        dataSource.saveStatus(status)
    }

    override suspend fun refreshStatus() = Unit

    override suspend fun clearStatus() {
        dataSource.saveStatus(UserStatus())
    }
}

