package org.nimio.app.feature.account.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.nimio.app.core.common.NimioResult
import org.nimio.app.core.network.AuthTokenDataSource
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.AccountSession
import org.nimio.app.feature.account.domain.LocalProfile
import retrofit2.HttpException
import javax.inject.Inject

class RemoteAccountRepository @Inject constructor(
    private val accountApi: AccountApi,
    private val authTokenDataSource: AuthTokenDataSource,
    private val profileDataSource: ProfilePreferencesDataSource,
    private val json: Json
) : AccountRepository {

    override fun observeSession(): Flow<AccountSession?> {
        return combine(
            authTokenDataSource.observeToken(),
            profileDataSource.observeProfile()
        ) { token, profile ->
            if (token.isNullOrBlank()) {
                null
            } else {
                AccountSession(
                    userId = profile.userId,
                    displayName = profile.displayName.ifBlank {
                        profile.username.ifBlank { "Nimio" }
                    },
                    isSignedIn = true
                )
            }
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): NimioResult<AccountSession> {
        return runCatching {
            val response = accountApi.register(
                RegisterRequestDto(
                    email = email.trim(),
                    password = password,
                    username = username.trim(),
                    displayName = displayName.trim()
                )
            )
            val payload = response.requireData()
            val existing = profileDataSource.observeProfile().first()
            authTokenDataSource.setToken(payload.token)
            saveMergedProfile(existing = existing, payload = payload)
            payload.toAccountSession()
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): NimioResult<AccountSession> {
        return runCatching {
            val response = accountApi.login(
                LoginRequestDto(
                    email = email.trim(),
                    password = password
                )
            )
            val payload = response.requireData()
            val existing = profileDataSource.observeProfile().first()
            authTokenDataSource.setToken(payload.token)
            saveMergedProfile(existing = existing, payload = payload)
            payload.toAccountSession()
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun refreshSession(): NimioResult<AccountSession?> {
        val existing = profileDataSource.observeProfile().first()
        if (authTokenDataSource.getToken().isNullOrBlank()) {
            return NimioResult.Success(null)
        }

        return runCatching {
            val response = accountApi.getMyProfile()
            val payload = response.requireData()
            val updatedProfile = existing.copy(
                userId = payload.user.id,
                email = payload.user.email,
                username = payload.profile.username,
                displayName = payload.profile.displayName,
                bio = payload.profile.bio ?: existing.bio,
                avatarUri = payload.profile.avatarUrl ?: existing.avatarUri,
                onboardingCompleted = true
            )
            profileDataSource.saveProfile(updatedProfile)
            updatedProfile.toAccountSession()
        }.recoverCatching { error ->
            if (error is HttpException && error.code() == 401) {
                signOut()
                null
            } else {
                throw error
            }
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it) }
        )
    }

    override suspend fun signOut() {
        authTokenDataSource.clearToken()
        profileDataSource.saveProfile(LocalProfile())
    }

    private suspend fun saveMergedProfile(
        existing: LocalProfile,
        payload: AuthPayloadDto
    ) {
        profileDataSource.saveProfile(
            existing.copy(
                userId = payload.user.id,
                email = payload.user.email,
                username = payload.profile.username,
                displayName = payload.profile.displayName,
                bio = payload.profile.bio ?: existing.bio,
                avatarUri = payload.profile.avatarUrl ?: existing.avatarUri,
                onboardingCompleted = true
            )
        )
    }
}

private fun ApiEnvelope<AuthPayloadDto>.requireData(): AuthPayloadDto {
    return if (success && data != null) {
        data
    } else {
        throw IllegalStateException(error?.message ?: "Unknown authentication error")
    }
}

private fun ApiEnvelope<ProfilePayloadDto>.requireData(): ProfilePayloadDto {
    return if (success && data != null) {
        data
    } else {
        throw IllegalStateException(error?.message ?: "Unknown profile error")
    }
}

private fun AuthPayloadDto.toAccountSession(): AccountSession {
    return AccountSession(
        userId = user.id,
        displayName = profile.displayName,
        isSignedIn = true
    )
}

private fun LocalProfile.toAccountSession(): AccountSession {
    return AccountSession(
        userId = userId,
        displayName = displayName.ifBlank { username.ifBlank { "Nimio" } },
        isSignedIn = onboardingCompleted
    )
}

private fun Throwable.toUserFacingAuthError(json: Json): Throwable {
    if (this !is HttpException) return this

    val parsedMessage = response()
        ?.errorBody()
        ?.string()
        ?.let { body ->
            runCatching {
                json.decodeFromString<ApiFailureEnvelope>(body).error?.message
            }.getOrNull()
        }

    val fallback = when (code()) {
        409 -> "That email or username is already taken."
        401 -> "Invalid email or password."
        else -> "Unable to complete this request right now."
    }

    return IllegalStateException(parsedMessage ?: fallback, this)
}

