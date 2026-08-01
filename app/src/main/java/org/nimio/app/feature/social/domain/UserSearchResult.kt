package org.nimio.app.feature.social.domain

data class UserSearchResult(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null
)

