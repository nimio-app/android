package org.nimio.app.feature.account.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.nimio.app.core.common.NimioResult
import org.nimio.app.core.network.AuthTokenDataSource
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.AccountSession
import org.nimio.app.feature.account.domain.LocalProfile
import retrofit2.HttpException
import java.io.File
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
            authTokenDataSource.clearLogoutNotice()
            authTokenDataSource.setToken(payload.token)
            payload.refreshToken?.let { authTokenDataSource.setRefreshToken(it) }
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
            authTokenDataSource.clearLogoutNotice()
            authTokenDataSource.setToken(payload.token)
            payload.refreshToken?.let { authTokenDataSource.setRefreshToken(it) }
            saveMergedProfile(existing = existing, payload = payload)
            payload.toAccountSession()
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun googleSignIn(idToken: String): NimioResult<AccountSession> {
        return runCatching {
            val response = accountApi.googleSignIn(
                GoogleSignInRequestDto(idToken = idToken.trim())
            )
            val payload = response.requireData()
            val existing = profileDataSource.observeProfile().first()
            authTokenDataSource.clearLogoutNotice()
            authTokenDataSource.setToken(payload.token)
            payload.refreshToken?.let { authTokenDataSource.setRefreshToken(it) }
            saveMergedProfile(existing = existing, payload = payload)
            payload.toAccountSession()
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun resendVerification(email: String): NimioResult<Unit> {
        return runCatching {
            accountApi.resendVerification(
                ResendVerificationRequestDto(email = email.trim())
            ).requireSuccess(fallbackMessage = "Unable to resend verification email")
        }.fold(
            onSuccess = { NimioResult.Success(Unit) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun verifyEmailToken(token: String): NimioResult<Unit> {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            return NimioResult.Error(IllegalStateException("Verification link is missing a token."))
        }

        return runCatching {
            runCatching {
                accountApi.verifyEmailPost(
                    VerifyEmailRequestDto(token = cleanToken)
                ).requireSuccess(fallbackMessage = "Unable to verify email")
            }.recoverCatching { error ->
                // Support backends that expose GET /v1/auth/verify-email?token=... instead of POST.
                if (error is HttpException && (error.code() == 404 || error.code() == 405)) {
                    accountApi.verifyEmailGet(cleanToken)
                        .requireSuccess(fallbackMessage = "Unable to verify email")
                } else {
                    throw error
                }
            }.getOrThrow()
        }.fold(
            onSuccess = { NimioResult.Success(Unit) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun updateProfile(
        username: String,
        displayName: String,
        bio: String
    ): NimioResult<Unit> {
        return runCatching {
            val response = accountApi.updateMyProfile(
                UpdateProfileRequestDto(
                    username = username.trim(),
                    displayName = displayName.trim(),
                    bio = bio.trim().ifBlank { null }
                )
            )
            val payload = response.requireData()
            val existing = profileDataSource.observeProfile().first()
            val payloadUser = payload.user
            val payloadProfile = payload.profile
            profileDataSource.saveProfile(
                existing.copy(
                    userId = payloadUser?.id ?: existing.userId,
                    email = payloadUser?.email ?: existing.email,
                    emailVerified = payloadUser?.emailVerified ?: existing.emailVerified,
                    username = payloadProfile?.username ?: existing.username,
                    displayName = payloadProfile?.displayName ?: existing.displayName,
                    bio = payloadProfile?.bio ?: existing.bio,
                    avatarUri = payloadProfile?.avatarUrl ?: existing.avatarUri,
                    onboardingCompleted = true
                )
            )
        }.fold(
            onSuccess = { NimioResult.Success(Unit) },
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
            val payloadUser = payload.user
            val payloadProfile = payload.profile
            val updatedProfile = existing.copy(
                userId = payloadUser?.id ?: existing.userId,
                email = payloadUser?.email ?: existing.email,
                emailVerified = payloadUser?.emailVerified ?: existing.emailVerified,
                username = payloadProfile?.username ?: existing.username,
                displayName = payloadProfile?.displayName ?: existing.displayName,
                bio = payloadProfile?.bio ?: existing.bio,
                avatarUri = payloadProfile?.avatarUrl ?: existing.avatarUri,
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
        authTokenDataSource.clearTokens()
        profileDataSource.saveProfile(LocalProfile())
    }

    override suspend fun uploadAvatar(filePath: String): NimioResult<String> {
        val file = filePath.toAvatarFileOrNull()
            ?: return NimioResult.Error(IllegalStateException("Avatar file not found: $filePath"))
        if (!file.exists() || !file.isFile) {
            return NimioResult.Error(IllegalStateException("Avatar file not found: $filePath"))
        }

        if (file.length() > 5 * 1024 * 1024) {
            return NimioResult.Error(IllegalStateException("Avatar file exceeds 5MB limit."))
        }

        return runCatching {
            val requestBody = file.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("avatar", file.name, requestBody)
            val response = accountApi.uploadAvatar(part)
            if (response.success && response.data != null) {
                response.data.avatarUrl
            } else {
                throw IllegalStateException(response.error?.message ?: "Avatar upload failed")
            }
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    override suspend fun deleteAvatar(): NimioResult<Unit> {
        return runCatching {
            accountApi.deleteAvatar().requireSuccess(fallbackMessage = "Unable to delete avatar")
        }.fold(
            onSuccess = { NimioResult.Success(Unit) },
            onFailure = { NimioResult.Error(it.toUserFacingAuthError(json)) }
        )
    }

    private suspend fun saveMergedProfile(
        existing: LocalProfile,
        payload: AuthPayloadDto
    ) {
        profileDataSource.saveProfile(
            existing.copy(
                userId = payload.user.id,
                email = payload.user.email,
                emailVerified = payload.user.emailVerified,
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

private fun <T> ApiEnvelope<T>.requireSuccess(fallbackMessage: String) {
    if (!success) {
        throw IllegalStateException(error?.message ?: fallbackMessage)
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

private fun String.toAvatarFileOrNull(): File? {
    if (isBlank()) return null

    return runCatching {
        val parsed = Uri.parse(this)
        when {
            parsed.scheme == "file" -> parsed.path?.let(::File)
            parsed.scheme.isNullOrBlank() -> File(this)
            else -> null
        }
    }.getOrNull()
}

