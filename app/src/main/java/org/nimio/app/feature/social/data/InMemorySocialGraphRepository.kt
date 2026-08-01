package org.nimio.app.feature.social.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.asStateFlow
import org.nimio.app.core.common.NimioResult
import org.nimio.app.feature.social.domain.ConnectionActionResult
import org.nimio.app.feature.social.domain.ConnectionStatus
import org.nimio.app.feature.social.domain.ConnectionSummary
import org.nimio.app.feature.social.domain.ConnectionTier
import org.nimio.app.feature.social.domain.SocialGraphRepository
import org.nimio.app.feature.social.domain.UserSearchResult

class InMemorySocialGraphRepository : SocialGraphRepository {
    private val connections = MutableStateFlow<List<ConnectionSummary>>(emptyList())

    override fun observeConnectionsCount(): Flow<Int> = flowOf(0)

    override fun observeConnections(): Flow<List<ConnectionSummary>> = connections.asStateFlow()

    override suspend fun refreshConnections(status: ConnectionStatus?): NimioResult<List<ConnectionSummary>> {
        return NimioResult.Success(connections.value)
    }

    override suspend fun requestConnection(
        toUserId: String,
        relationshipTier: ConnectionTier
    ): NimioResult<ConnectionActionResult> {
        return NimioResult.Error(IllegalStateException("Not available in in-memory mode"))
    }

    override suspend fun acceptConnection(fromUserId: String): NimioResult<ConnectionActionResult> {
        return NimioResult.Error(IllegalStateException("Not available in in-memory mode"))
    }

    override suspend fun rejectConnection(fromUserId: String): NimioResult<ConnectionActionResult> {
        return NimioResult.Error(IllegalStateException("Not available in in-memory mode"))
    }

    override suspend fun blockUser(userId: String): NimioResult<ConnectionActionResult> {
        return NimioResult.Error(IllegalStateException("Not available in in-memory mode"))
    }

    override suspend fun updateRelationshipTier(
        friendId: String,
        relationshipTier: ConnectionTier
    ): NimioResult<ConnectionActionResult> {
        return NimioResult.Error(IllegalStateException("Not available in in-memory mode"))
    }

    override suspend fun getConnectionStatus(userId: String): NimioResult<ConnectionSummary?> {
        return NimioResult.Success(null)
    }

    override suspend fun removeConnection(friendId: String): NimioResult<Unit> {
        return NimioResult.Success(Unit)
    }

    override suspend fun searchUsers(query: String, limit: Int): NimioResult<List<UserSearchResult>> {
        return NimioResult.Success(emptyList())
    }
}

