package org.nimio.app.feature.account.domain

import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeSession(): Flow<AccountSession?>
    suspend fun signOut()
}

