package org.nimio.app.core.network

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val tokenDataSource: AuthTokenDataSource
) : Interceptor {

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
        if (response.code == 401) {
            android.util.Log.w("AuthInterceptor", "401 on $path with token: ${token.take(10)}...")
        }
        return response
    }

    private fun requiresAuthorization(path: String): Boolean {
        return path.startsWith("/v1/me/") || path.startsWith("/v1/feed/")
    }
}

