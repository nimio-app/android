package org.nimio.app.feature.account.data

import kotlinx.coroutines.flow.Flow
import org.nimio.app.feature.account.domain.LocalProfile
import org.nimio.app.feature.account.domain.LocalProfileRepository

class DefaultLocalProfileRepository(
    private val dataSource: ProfilePreferencesDataSource
) : LocalProfileRepository {

    override fun observeProfile(): Flow<LocalProfile> = dataSource.observeProfile()

    override suspend fun saveProfile(profile: LocalProfile) {
        dataSource.saveProfile(profile)
    }

    override suspend fun completeOnboarding(displayName: String, bio: String, avatarUri: String?) {
        dataSource.saveProfile(
            LocalProfile(
                displayName = displayName.trim(),
                bio = bio.trim(),
                avatarUri = avatarUri,
                onboardingCompleted = true
            )
        )
    }
}

