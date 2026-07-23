package org.nimio.app.feature.account.domain

import kotlinx.coroutines.flow.Flow

interface LocalProfileRepository {
    fun observeProfile(): Flow<LocalProfile>
    suspend fun saveProfile(profile: LocalProfile)
    suspend fun completeOnboarding(displayName: String, bio: String, avatarUri: String?)
}

