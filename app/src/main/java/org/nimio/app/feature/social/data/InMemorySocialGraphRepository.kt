package org.nimio.app.feature.social.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.nimio.app.feature.social.domain.SocialGraphRepository

class InMemorySocialGraphRepository : SocialGraphRepository {
    override fun observeConnectionsCount(): Flow<Int> = flowOf(0)
}

