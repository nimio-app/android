package org.nimio.app.core.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val AUTH_DATASTORE_NAME = "nimio_auth"

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = AUTH_DATASTORE_NAME)

private object AuthPreferencesKeys {
    val accessToken = stringPreferencesKey("access_token")
}

class AuthTokenDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore: DataStore<Preferences> = context.authDataStore

    suspend fun getToken(): String? {
        return dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences -> preferences[AuthPreferencesKeys.accessToken] }
            .first()
    }

    suspend fun setToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AuthPreferencesKeys.accessToken] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferencesKeys.accessToken)
        }
    }
}

