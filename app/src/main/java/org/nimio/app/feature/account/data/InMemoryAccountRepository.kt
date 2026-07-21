package org.nimio.app.feature.account.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.AccountSession

class InMemoryAccountRepository : AccountRepository {
    private val session = MutableStateFlow<AccountSession?>(null)

    override fun observeSession(): Flow<AccountSession?> = session.asStateFlow()

    override suspend fun signOut() {
        session.value = null
    }
}

