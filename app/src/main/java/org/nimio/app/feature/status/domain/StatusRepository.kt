package org.nimio.app.feature.status.domain

import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    fun observeStatus(): Flow<UserStatus>
    fun observeActiveStatuses(): Flow<Map<VisibilityTier, UserStatus>>
    suspend fun saveStatus(status: UserStatus)
    suspend fun refreshStatus()
    suspend fun clearStatus()
}

