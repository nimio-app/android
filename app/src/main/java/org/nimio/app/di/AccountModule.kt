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
}

