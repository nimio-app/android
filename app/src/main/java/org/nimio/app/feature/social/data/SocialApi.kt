package org.nimio.app.feature.social.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
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

    @PUT("v1/connections/{connectionId}/tier")
    suspend fun updateRelationshipTier(
        @Path("connectionId") connectionId: String,
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

    @GET("v1/feed/status")
    suspend fun getVisibleStatuses(): ApiEnvelope<VisibleStatusesPayloadDto>
}

@Serializable
data class ConnectionRequestDto(
    @SerialName("to_user_id") val toUserId: String,
    @SerialName("relationship_tier") val relationshipTier: String = "ALL"
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
    @Serializable(with = ConnectionItemListSerializer::class)
    val connections: List<ConnectionItemDto> = emptyList(),
    val count: Int = 0
)

@Serializable
data class ConnectionItemDto(
    val connection: ConnectionDto,
    val profile: SocialProfileDto,
    @SerialName("initiated_by_me") val initiatedByMe: Boolean = false,
    @SerialName("counterpart_user_id") val counterpartUserId: String? = null,
    @SerialName("my_tier_for_them") val myTierForThem: String? = null,
    @SerialName("their_tier_for_me") val theirTierForMe: String? = null,
    @SerialName("pending_action_hint") val pendingActionHint: String? = null
)

@Serializable
data class ConnectionStatusPayloadDto(
    val connection: ConnectionDto? = null,
    val status: String
)

@Serializable
data class UserSearchPayloadDto(
    @Serializable(with = UserSearchItemListSerializer::class)
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
data class VisibleStatusesPayloadDto(
    @Serializable(with = VisibleStatusItemListSerializer::class)
    val statuses: List<VisibleStatusItemDto> = emptyList(),
    val count: Int = 0
)

@Serializable
data class VisibleStatusItemDto(
    val status: FeedStatusDto? = null,
    val profile: SocialProfileDto? = null,
    @SerialName("user_id") val userId: String? = null,
    val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("availability_type") val availabilityType: String? = null,
    val note: String? = null,
    @SerialName("visibility_tier") val visibilityTier: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
data class FeedStatusDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("availability_type") val availabilityType: String? = null,
    val note: String? = null,
    @SerialName("visibility_tier") val visibilityTier: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
data class ConnectionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String,
    @SerialName("relationship_tier") val relationshipTier: String = "ALL",
    @SerialName("user_tier") val userTier: String? = null,
    @SerialName("friend_tier") val friendTier: String? = null,
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

private object ConnectionItemListSerializer : JsonTransformingSerializer<List<ConnectionItemDto>>(
    ListSerializer(ConnectionItemDto.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element as? JsonArray ?: JsonArray(emptyList())
    }
}

private object UserSearchItemListSerializer : JsonTransformingSerializer<List<UserSearchItemDto>>(
    ListSerializer(UserSearchItemDto.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element as? JsonArray ?: JsonArray(emptyList())
    }
}

private object VisibleStatusItemListSerializer : JsonTransformingSerializer<List<VisibleStatusItemDto>>(
    ListSerializer(VisibleStatusItemDto.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return element as? JsonArray ?: JsonArray(emptyList())
    }
}


