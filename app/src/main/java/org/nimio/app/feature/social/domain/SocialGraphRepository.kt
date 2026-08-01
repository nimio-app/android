package org.nimio.app.feature.social.domain

import kotlinx.coroutines.flow.Flow
import org.nimio.app.core.common.NimioResult

interface SocialGraphRepository {
    fun observeConnectionsCount(): Flow<Int>

    fun observeConnections(): Flow<List<ConnectionSummary>>

    suspend fun refreshConnections(
        status: ConnectionStatus? = ConnectionStatus.ACCEPTED
    ): NimioResult<List<ConnectionSummary>>

    suspend fun requestConnection(
        toUserId: String,
        relationshipTier: ConnectionTier = ConnectionTier.MUTUAL
    ): NimioResult<ConnectionActionResult>

    suspend fun acceptConnection(fromUserId: String): NimioResult<ConnectionActionResult>

    suspend fun rejectConnection(fromUserId: String): NimioResult<ConnectionActionResult>

    suspend fun blockUser(userId: String): NimioResult<ConnectionActionResult>

    suspend fun updateRelationshipTier(
        friendId: String,
        relationshipTier: ConnectionTier
    ): NimioResult<ConnectionActionResult>

    suspend fun getConnectionStatus(userId: String): NimioResult<ConnectionSummary?>

    suspend fun removeConnection(friendId: String): NimioResult<Unit>

    suspend fun searchUsers(
        query: String,
        limit: Int = 20
    ): NimioResult<List<UserSearchResult>>
}

