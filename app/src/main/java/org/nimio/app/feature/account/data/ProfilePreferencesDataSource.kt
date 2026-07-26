package org.nimio.app.feature.account.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.nimio.app.feature.account.domain.LocalProfile

private const val PROFILE_DATASTORE_NAME = "nimio_profile"

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = PROFILE_DATASTORE_NAME)

private object ProfilePreferencesKeys {
    val userId = stringPreferencesKey("user_id")
    val email = stringPreferencesKey("email")
    val emailVerified = booleanPreferencesKey("email_verified")
    val username = stringPreferencesKey("username")
    val displayName = stringPreferencesKey("display_name")
    val bio = stringPreferencesKey("bio")
    val avatarUri = stringPreferencesKey("avatar_uri")
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
}

class ProfilePreferencesDataSource(
    context: Context
) {
    private val dataStore: DataStore<Preferences> = context.profileDataStore

    fun observeProfile(): Flow<LocalProfile> {
        return dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                LocalProfile(
                    userId = preferences[ProfilePreferencesKeys.userId].orEmpty(),
                    email = preferences[ProfilePreferencesKeys.email].orEmpty(),
                    emailVerified = preferences[ProfilePreferencesKeys.emailVerified] ?: false,
                    username = preferences[ProfilePreferencesKeys.username].orEmpty(),
                    displayName = preferences[ProfilePreferencesKeys.displayName].orEmpty(),
                    bio = preferences[ProfilePreferencesKeys.bio].orEmpty(),
                    avatarUri = preferences[ProfilePreferencesKeys.avatarUri],
                    onboardingCompleted = preferences[ProfilePreferencesKeys.onboardingCompleted] ?: false
                )
            }
    }

    suspend fun saveProfile(profile: LocalProfile) {
        dataStore.edit { preferences ->
            preferences[ProfilePreferencesKeys.userId] = profile.userId
            preferences[ProfilePreferencesKeys.email] = profile.email
            preferences[ProfilePreferencesKeys.emailVerified] = profile.emailVerified
            preferences[ProfilePreferencesKeys.username] = profile.username
            preferences[ProfilePreferencesKeys.displayName] = profile.displayName
            preferences[ProfilePreferencesKeys.bio] = profile.bio
            profile.avatarUri?.let {
                preferences[ProfilePreferencesKeys.avatarUri] = it
            } ?: run {
                preferences.remove(ProfilePreferencesKeys.avatarUri)
            }
            preferences[ProfilePreferencesKeys.onboardingCompleted] = profile.onboardingCompleted
        }
    }
}

