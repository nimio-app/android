package org.nimio.app.feature.status.domain

import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    fun observeStatus(): Flow<UserStatus>
    suspend fun saveStatus(status: UserStatus)
}

