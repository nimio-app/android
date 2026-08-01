package org.nimio.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.nimio.app.feature.account.data.DefaultLocalProfileRepository
import org.nimio.app.feature.account.data.ProfilePreferencesDataSource
import org.nimio.app.feature.account.data.RemoteAccountRepository
import org.nimio.app.feature.account.domain.AccountRepository
import org.nimio.app.feature.account.domain.LocalProfileRepository
import org.nimio.app.feature.status.data.RemoteStatusRepository
import org.nimio.app.feature.status.data.StatusPreferencesDataSource
import org.nimio.app.feature.status.domain.StatusRepository
import org.nimio.app.feature.social.data.RemoteSocialGraphRepository
import org.nimio.app.feature.social.domain.SocialGraphRepository

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideProfilePreferencesDataSource(
        @ApplicationContext context: Context
    ): ProfilePreferencesDataSource {
        return ProfilePreferencesDataSource(context)
    }

    @Provides
    @Singleton
    fun provideLocalProfileRepository(
        dataSource: ProfilePreferencesDataSource
    ): LocalProfileRepository {
        return DefaultLocalProfileRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        repository: RemoteAccountRepository
    ): AccountRepository {
        return repository
    }

    @Provides
    @Singleton
    fun provideSocialGraphRepository(
        repository: RemoteSocialGraphRepository
    ): SocialGraphRepository {
        return repository
    }

    @Provides
    @Singleton
    fun provideStatusPreferencesDataSource(
        @ApplicationContext context: Context
    ): StatusPreferencesDataSource {
        return StatusPreferencesDataSource(context)
    }

    @Provides
    @Singleton
    fun provideStatusRepository(
        repository: RemoteStatusRepository
    ): StatusRepository {
        return repository
    }
}

