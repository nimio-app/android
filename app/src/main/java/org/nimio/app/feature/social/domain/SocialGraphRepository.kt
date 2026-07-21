package org.nimio.app.feature.social.domain

import kotlinx.coroutines.flow.Flow

interface SocialGraphRepository {
    fun observeConnectionsCount(): Flow<Int>
}

