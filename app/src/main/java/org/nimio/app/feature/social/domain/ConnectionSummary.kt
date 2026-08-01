package org.nimio.app.feature.social.domain

data class ConnectionSummary(
    val id: String,
    val userId: String,
    val friendId: String,
    val counterpartUserId: String,
    val relationshipTier: ConnectionTier,
    val status: ConnectionStatus,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val initiatedByMe: Boolean = false,
    val pendingActionHint: PendingActionHint? = null,
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null
)

