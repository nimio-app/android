package org.nimio.app.feature.account.domain

import kotlinx.coroutines.flow.Flow
import org.nimio.app.core.common.NimioResult

interface AccountRepository {
    fun observeSession(): Flow<AccountSession?>
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): NimioResult<AccountSession>

    suspend fun signIn(
        email: String,
        password: String
    ): NimioResult<AccountSession>

    suspend fun googleSignIn(idToken: String): NimioResult<AccountSession>

    suspend fun resendVerification(email: String): NimioResult<Unit>

    suspend fun verifyEmailToken(token: String): NimioResult<Unit>

    suspend fun updateProfile(
        username: String,
        displayName: String,
        bio: String
    ): NimioResult<Unit>

    suspend fun uploadAvatar(filePath: String): NimioResult<String>

    suspend fun deleteAvatar(): NimioResult<Unit>

    suspend fun refreshSession(): NimioResult<AccountSession?>
    suspend fun signOut()
}

