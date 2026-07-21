package org.nimio.app.feature.status.data

import kotlinx.coroutines.flow.Flow
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.status.domain.UserStatus

class DefaultStatusRepository(
    private val dataSource: StatusPreferencesDataSource
) : StatusRepository {
    override fun observeStatus(): Flow<UserStatus> = dataSource.observeStatus()

    override suspend fun saveStatus(status: UserStatus) {
        dataSource.saveStatus(status)
    }
}

