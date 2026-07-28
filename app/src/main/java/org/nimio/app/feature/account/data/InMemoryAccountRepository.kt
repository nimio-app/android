package org.nimio.app.feature.account.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nimio.app.core.common.NimioResult
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.AccountSession

class InMemoryAccountRepository : AccountRepository {
    private val session = MutableStateFlow<AccountSession?>(null)

    override fun observeSession(): Flow<AccountSession?> = session.asStateFlow()

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): NimioResult<AccountSession> {
        val newSession = AccountSession(
            userId = username,
            displayName = displayName,
            isSignedIn = true
        )
        session.value = newSession
        return NimioResult.Success(newSession)
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): NimioResult<AccountSession> {
        val newSession = AccountSession(
            userId = email,
            displayName = email.substringBefore('@'),
            isSignedIn = true
        )
        session.value = newSession
        return NimioResult.Success(newSession)
    }

    override suspend fun resendVerification(email: String): NimioResult<Unit> {
        return NimioResult.Success(Unit)
    }

    override suspend fun verifyEmailToken(token: String): NimioResult<Unit> {
        return NimioResult.Success(Unit)
    }

    override suspend fun refreshSession(): NimioResult<AccountSession?> {
        return NimioResult.Success(session.value)
    }

    override suspend fun signOut() {
        session.value = null
    }
}

