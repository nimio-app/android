package org.nimio.app.core.network

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor @Inject constructor(
    private val tokenDataSource: AuthTokenDataSource
) : Interceptor {

    private val refreshMutex = Mutex()
    private val refreshClient = OkHttpClient()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        if (!requiresAuthorization(path)) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenDataSource.getToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authorizedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authorizedRequest)
        if (response.code != 401) {
            return response
        }

        android.util.Log.w("AuthInterceptor", "401 on $path, attempting token refresh")
        val refreshedToken = runBlocking {
            refreshAccessTokenIfPossible(
                failedAccessToken = token,
                originalRequest = originalRequest
            )
        }

        if (refreshedToken.isNullOrBlank()) {
            return response
        }

        response.close()
        val retriedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $refreshedToken")
            .build()

        return chain.proceed(retriedRequest)
    }

    private suspend fun refreshAccessTokenIfPossible(
        failedAccessToken: String,
        originalRequest: Request
    ): String? {
        return refreshMutex.withLock {
            // Another in-flight request may already have refreshed session tokens.
            val latestToken = tokenDataSource.getToken()
            if (!latestToken.isNullOrBlank() && latestToken != failedAccessToken) {
                return latestToken
            }

            val refreshToken = tokenDataSource.getRefreshToken().orEmpty().trim()
            if (refreshToken.isBlank()) {
                tokenDataSource.setLogoutNotice(AuthTokenDataSource.LOGOUT_NOTICE_SESSION_EXPIRED)
                tokenDataSource.clearTokens()
                return null
            }

            val refreshAttempt = runCatching {
                performRefreshRequest(originalRequest, refreshToken)
            }.getOrNull() ?: return null

            when {
                refreshAttempt.success && refreshAttempt.accessToken != null -> {
                    tokenDataSource.setToken(refreshAttempt.accessToken)
                    refreshAttempt.refreshToken?.let { tokenDataSource.setRefreshToken(it) }
                    tokenDataSource.clearLogoutNotice()
                    return refreshAttempt.accessToken
                }

                refreshAttempt.shouldSignOut -> {
                    tokenDataSource.setLogoutNotice(AuthTokenDataSource.LOGOUT_NOTICE_SESSION_EXPIRED)
                    tokenDataSource.clearTokens()
                    return null
                }

                else -> {
                    // Keep existing tokens for transient failures and try again later.
                    return null
                }
            }
        }
    }

    private fun performRefreshRequest(
        originalRequest: Request,
        refreshToken: String
    ): RefreshAttempt {
        val refreshUrl = originalRequest.url.newBuilder()
            .encodedPath("/v1/auth/refresh")
            .query(null)
            .build()

        val requestBody = JSONObject()
            .put("refresh_token", refreshToken)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val refreshRequest = Request.Builder()
            .url(refreshUrl)
            .post(requestBody)
            .build()

        refreshClient.newCall(refreshRequest).execute().use { response ->
            val payload = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val shouldSignOut = response.code == 401 || response.code == 403
                return RefreshAttempt(success = false, shouldSignOut = shouldSignOut)
            }

            val root = runCatching { JSONObject(payload) }.getOrNull()
                ?: return RefreshAttempt(success = false)

            if (!root.optBoolean("success", false)) {
                return RefreshAttempt(success = false)
            }

            val data = root.optJSONObject("data")
            val accessToken = data?.optString("token")?.takeIf { it.isNotBlank() }
            val rotatedRefreshToken = data?.optString("refresh_token")?.takeIf { it.isNotBlank() }

            return RefreshAttempt(
                success = accessToken != null,
                accessToken = accessToken,
                refreshToken = rotatedRefreshToken,
                shouldSignOut = false
            )
        }
    }

    private fun requiresAuthorization(path: String): Boolean {
        return path.startsWith("/v1/me") ||
            path.startsWith("/v1/feed/") ||
            path.startsWith("/v1/connections") ||
            path.startsWith("/v1/users/")
    }
}

private data class RefreshAttempt(
    val success: Boolean,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val shouldSignOut: Boolean = false
)

