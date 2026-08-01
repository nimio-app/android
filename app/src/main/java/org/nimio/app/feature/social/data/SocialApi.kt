package org.nimio.app.feature.social.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import org.nimio.app.feature.account.data.ApiEnvelope

interface SocialApi {
    @POST("v1/connections/request")
    suspend fun requestConnection(
        @Body request: ConnectionRequestDto
    ): ApiEnvelope<ConnectionActionPayloadDto>

    @POST("v1/connections/accept")
    suspend fun acceptConnection(
        @Body request: AcceptConnectionRequestDto
    ): ApiEnvelope<ConnectionActionPayloadDto>

    @POST("v1/connections/reject")
    suspend fun rejectConnection(
        @Body request: RejectConnectionRequestDto
    ): ApiEnvelope<ConnectionActionPayloadDto>

    @POST("v1/connections/block")
    suspend fun blockConnection(
        @Body request: BlockConnectionRequestDto
    ): ApiEnvelope<ConnectionActionPayloadDto>

    @PUT("v1/connections/tier")
    suspend fun updateRelationshipTier(
        @Body request: UpdateRelationshipTierRequestDto
    ): ApiEnvelope<ConnectionActionPayloadDto>

    @GET("v1/connections")
    suspend fun getConnections(
        @Query("status") status: String? = null
    ): ApiEnvelope<ConnectionsListPayloadDto>

    @GET("v1/connections/status/{userId}")
    suspend fun getConnectionStatus(
        @Path("userId") userId: String
    ): ApiEnvelope<ConnectionStatusPayloadDto>

    @DELETE("v1/connections/{friendId}")
    suspend fun removeConnection(
        @Path("friendId") friendId: String
    ): ApiEnvelope<MessagePayloadDto>

    @GET("v1/users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): ApiEnvelope<UserSearchPayloadDto>
}

@Serializable
data class ConnectionRequestDto(
    @SerialName("to_user_id") val toUserId: String,
    @SerialName("relationship_tier") val relationshipTier: String = "MUTUAL"
)

@Serializable
data class AcceptConnectionRequestDto(
    @SerialName("from_user_id") val fromUserId: String
)

@Serializable
data class RejectConnectionRequestDto(
    @SerialName("from_user_id") val fromUserId: String
)

@Serializable
data class BlockConnectionRequestDto(
    @SerialName("user_id") val userId: String
)

@Serializable
data class UpdateRelationshipTierRequestDto(
    @SerialName("friend_id") val friendId: String,
    @SerialName("relationship_tier") val relationshipTier: String
)

@Serializable
data class MessagePayloadDto(
    val message: String
)

@Serializable
data class ConnectionActionPayloadDto(
    val connection: ConnectionDto,
    val message: String? = null
)

@Serializable
data class ConnectionsListPayloadDto(
    val connections: List<ConnectionItemDto> = emptyList(),
    val count: Int = 0
)

@Serializable
data class ConnectionItemDto(
    val connection: ConnectionDto,
    val profile: SocialProfileDto,
    @SerialName("initiated_by_me") val initiatedByMe: Boolean = false,
    @SerialName("counterpart_user_id") val counterpartUserId: String? = null,
    @SerialName("pending_action_hint") val pendingActionHint: String? = null
)

@Serializable
data class ConnectionStatusPayloadDto(
    val connection: ConnectionDto? = null,
    val status: String
)

@Serializable
data class UserSearchPayloadDto(
    val users: List<UserSearchItemDto> = emptyList(),
    val count: Int = 0
)

@Serializable
data class UserSearchItemDto(
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class ConnectionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String,
    @SerialName("relationship_tier") val relationshipTier: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SocialProfileDto(
    @SerialName("user_id") val userId: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null
)


