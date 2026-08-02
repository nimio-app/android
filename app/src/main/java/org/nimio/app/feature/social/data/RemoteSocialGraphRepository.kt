package org.nimio.app.feature.social.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nimio.app.core.common.NimioResult
import org.nimio.app.feature.account.data.ApiEnvelope
import org.nimio.app.feature.account.data.ApiFailureEnvelope
import org.nimio.app.feature.account.domain.LocalProfileRepository
import org.nimio.app.feature.social.domain.ConnectionActionResult
import org.nimio.app.feature.social.domain.ConnectionStatus
import org.nimio.app.feature.social.domain.ConnectionSummary
import org.nimio.app.feature.social.domain.ConnectionTier
import org.nimio.app.feature.social.domain.PendingActionHint
import org.nimio.app.feature.social.domain.SocialGraphRepository
import org.nimio.app.feature.social.domain.UserSearchResult
import retrofit2.HttpException
import javax.inject.Inject

class RemoteSocialGraphRepository @Inject constructor(
    private val socialApi: SocialApi,
    private val localProfileRepository: LocalProfileRepository,
    private val json: Json
) : SocialGraphRepository {
    private val connectionsState = MutableStateFlow<List<ConnectionSummary>>(emptyList())

    override fun observeConnectionsCount(): Flow<Int> = connectionsState.map { it.size }

    override fun observeConnections(): Flow<List<ConnectionSummary>> = connectionsState.asStateFlow()

    override suspend fun refreshConnections(status: ConnectionStatus?): NimioResult<List<ConnectionSummary>> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.getConnections(status = status?.name)
            val payload = response.requireData()
            val items = payload.connections.map { it.toDomain(currentUserId) }
            connectionsState.value = items
            items
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun requestConnection(
        toUserId: String,
        relationshipTier: ConnectionTier
    ): NimioResult<ConnectionActionResult> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.requestConnection(
                ConnectionRequestDto(
                    toUserId = toUserId,
                    relationshipTier = relationshipTier.apiValue
                )
            )
            val payload = response.requireData()
            payload.toDomain(currentUserId)
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun acceptConnection(fromUserId: String): NimioResult<ConnectionActionResult> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.acceptConnection(
                AcceptConnectionRequestDto(fromUserId = fromUserId)
            )
            response.requireData().toDomain(currentUserId)
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun rejectConnection(fromUserId: String): NimioResult<ConnectionActionResult> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.rejectConnection(
                RejectConnectionRequestDto(fromUserId = fromUserId)
            )
            response.requireData().toDomain(currentUserId)
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun blockUser(userId: String): NimioResult<ConnectionActionResult> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.blockConnection(
                BlockConnectionRequestDto(userId = userId)
            )
            response.requireData().toDomain(currentUserId)
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun updateRelationshipTier(
        connectionId: String,
        relationshipTier: ConnectionTier
    ): NimioResult<ConnectionActionResult> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.updateRelationshipTier(
                connectionId = connectionId,
                UpdateRelationshipTierRequestDto(
                    relationshipTier = relationshipTier.apiValue
                )
            )
            response.requireData().toDomain(currentUserId)
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun getConnectionStatus(userId: String): NimioResult<ConnectionSummary?> {
        return runCatching {
            val currentUserId = localProfileRepository.observeProfile().first().userId.trim()
            val response = socialApi.getConnectionStatus(userId)
            val payload = response.requireData()
            payload.connection?.toDomain(currentUserId)
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun removeConnection(friendId: String): NimioResult<Unit> {
        return runCatching {
            socialApi.removeConnection(friendId)
                .requireSuccess("Connection removed")
        }.fold(
            onSuccess = { NimioResult.Success(Unit) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    override suspend fun searchUsers(query: String, limit: Int): NimioResult<List<UserSearchResult>> {
        return runCatching {
            val currentProfile = localProfileRepository.observeProfile().first()
            val currentUserId = currentProfile.userId.trim()
            val currentUsername = currentProfile.username.trim().lowercase()
            val response = socialApi.searchUsers(query = query.trim(), limit = limit)
            response.requireData()
                .users
                .map { it.toDomain() }
                .filterNot { candidate ->
                    (currentUserId.isNotBlank() && candidate.userId == currentUserId) ||
                        (currentUsername.isNotBlank() && candidate.username.trim().lowercase() == currentUsername)
                }
        }.fold(
            onSuccess = { NimioResult.Success(it) },
            onFailure = { NimioResult.Error(it.toSocialError(json)) }
        )
    }

    private fun ApiEnvelope<ConnectionsListPayloadDto>.requireData(): ConnectionsListPayloadDto {
        return if (success && data != null) data else throw IllegalStateException(error?.message ?: "Unable to load connections")
    }

    private fun ApiEnvelope<ConnectionActionPayloadDto>.requireData(): ConnectionActionPayloadDto {
        return if (success && data != null) data else throw IllegalStateException(error?.message ?: "Unable to update connection")
    }

    private fun ApiEnvelope<ConnectionStatusPayloadDto>.requireData(): ConnectionStatusPayloadDto {
        return if (success && data != null) data else throw IllegalStateException(error?.message ?: "Unable to load connection status")
    }

    private fun ApiEnvelope<UserSearchPayloadDto>.requireData(): UserSearchPayloadDto {
        return if (success && data != null) data else throw IllegalStateException(error?.message ?: "Unable to search users")
    }

    private fun ApiEnvelope<MessagePayloadDto>.requireSuccess(fallback: String) {
        if (!success) {
            throw IllegalStateException(error?.message ?: fallback)
        }
    }

    private fun ConnectionActionPayloadDto.toDomain(currentUserId: String): ConnectionActionResult {
        return ConnectionActionResult(
            connection = connection.toDomain(currentUserId),
            message = message
        )
    }

    private fun ConnectionDto.toDomain(currentUserId: String): ConnectionSummary {
        val counterpart = deriveCounterpartUserId(currentUserId)
        val normalizedTier = relationshipTier.toConnectionTier()
        return ConnectionSummary(
            id = id,
            userId = userId,
            friendId = friendId,
            counterpartUserId = counterpart,
            relationshipTier = normalizedTier,
            myTierForThem = normalizedTier,
            theirTierForMe = ConnectionTier.ALL,
            status = runCatching { ConnectionStatus.valueOf(status) }.getOrDefault(ConnectionStatus.NONE),
            createdAt = createdAt,
            updatedAt = updatedAt,
            initiatedByMe = currentUserId.isNotBlank() && userId == currentUserId
        )
    }

    private fun ConnectionItemDto.toDomain(currentUserId: String): ConnectionSummary {
        val fallbackCounterpart = connection.deriveCounterpartUserId(currentUserId)
        val counterpart = counterpartUserId?.takeIf { it.isNotBlank() } ?: fallbackCounterpart
        val actionHint = pendingActionHint
            ?.trim()
            ?.uppercase()
            ?.let { value -> runCatching { PendingActionHint.valueOf(value) }.getOrNull() }

        return connection.toDomain(currentUserId).copy(
            friendId = counterpart,
            counterpartUserId = counterpart,
            initiatedByMe = initiatedByMe,
            myTierForThem = myTierForThem.toConnectionTier(),
            theirTierForMe = theirTierForMe.toConnectionTier(),
            pendingActionHint = actionHint,
            username = profile.username,
            displayName = profile.displayName,
            avatarUrl = profile.avatarUrl
        )
    }

    private fun String?.toConnectionTier(): ConnectionTier {
        val normalized = this?.trim()?.uppercase().orEmpty()
        if (normalized == "MUTUAL") return ConnectionTier.ALL
        return runCatching { ConnectionTier.valueOf(normalized) }.getOrDefault(ConnectionTier.ALL)
    }

    private fun ConnectionDto.deriveCounterpartUserId(currentUserId: String): String {
        if (currentUserId.isBlank()) return friendId
        return if (userId == currentUserId) friendId else userId
    }

    private fun UserSearchItemDto.toDomain(): UserSearchResult {
        return UserSearchResult(
            userId = userId,
            username = username,
            displayName = displayName.orEmpty(),
            avatarUrl = avatarUrl
        )
    }

    private fun Throwable.toSocialError(json: Json): Throwable {
        if (this !is HttpException) return this
        val parsed = response()?.errorBody()?.string()?.let { body ->
            runCatching { json.decodeFromString<ApiFailureEnvelope>(body).error?.message }.getOrNull()
        }
        val fallback = when (code()) {
            401 -> "You need to sign in again."
            403 -> "You do not have permission to do that."
            404 -> "That user or connection could not be found."
            else -> "Could not complete that social action right now."
        }
        return IllegalStateException(parsed ?: fallback, this)
    }
}




