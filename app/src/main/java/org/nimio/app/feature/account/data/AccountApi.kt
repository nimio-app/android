package org.nimio.app.feature.account.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AccountApi {
    @POST("v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): ApiEnvelope<AuthPayloadDto>

    @POST("v1/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): ApiEnvelope<AuthPayloadDto>

    @POST("v1/auth/resend-verification")
    suspend fun resendVerification(
        @Body request: ResendVerificationRequestDto
    ): ApiEnvelope<Map<String, String>>

    @GET("v1/me/profile")
    suspend fun getMyProfile(): ApiEnvelope<ProfilePayloadDto>
}

