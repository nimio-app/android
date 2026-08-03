package org.nimio.app.feature.social.domain

import org.nimio.app.feature.social.domain.PendingActionHint

data class ConnectionSummary(
    val id: String,
    val userId: String,
    val friendId: String,
    val counterpartUserId: String,
    val relationshipTier: ConnectionTier,
    val myTierForThem: ConnectionTier = relationshipTier,
    val theirTierForMe: ConnectionTier = ConnectionTier.ALL,
    val status: ConnectionStatus,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val initiatedByMe: Boolean = false,
    val pendingActionHint: PendingActionHint? = null,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null
)

