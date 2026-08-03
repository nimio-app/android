package org.nimio.app.feature.status.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.nimio.app.feature.account.data.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

interface StatusApi {
    @PUT("v1/me/status")
    suspend fun updateMyStatus(
        @Body request: UpdateStatusRequestDto
    ): ApiEnvelope<StatusPayloadDto>

    @GET("v1/me/status")
    suspend fun getMyStatus(): ApiEnvelope<StatusPayloadDto>

    @GET("v1/me/statuses")
    suspend fun getMyStatuses(): ApiEnvelope<StatusesPayloadDto>

    @DELETE("v1/me/status")
    suspend fun clearMyStatus(): ApiEnvelope<StatusPayloadDto>
}

@Serializable
data class UpdateStatusRequestDto(
    @SerialName("availability_type") val availabilityType: String,
    val note: String,
    @SerialName("visibility_tier") val visibilityTier: String,
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
data class StatusPayloadDto(
    val status: StatusDto? = null
)

@Serializable
data class StatusesPayloadDto(
    val statuses: List<StatusDto> = emptyList(),
    val count: Int = 0
)

@Serializable
data class StatusDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("availability_type") val availabilityType: String,
    val note: String? = null,
    @SerialName("visibility_tier") val visibilityTier: String = "ALL_CONNECTIONS",
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

