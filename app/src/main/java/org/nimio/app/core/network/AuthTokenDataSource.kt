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
    val refreshToken = stringPreferencesKey("refresh_token")
    val logoutNotice = stringPreferencesKey("logout_notice")
}

class AuthTokenDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        const val LOGOUT_NOTICE_SESSION_EXPIRED = "session_expired"
    }

    private val dataStore: DataStore<Preferences> = context.authDataStore

    fun observeToken() = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences[AuthPreferencesKeys.accessToken] }

    fun observeLogoutNotice() = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences[AuthPreferencesKeys.logoutNotice] }

    fun observeRefreshToken() = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences[AuthPreferencesKeys.refreshToken] }

    suspend fun getToken(): String? {
        return observeToken().first()
    }

    suspend fun getLogoutNotice(): String? {
        return observeLogoutNotice().first()
    }

    suspend fun getRefreshToken(): String? {
        return observeRefreshToken().first()
    }

    suspend fun setToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AuthPreferencesKeys.accessToken] = token
        }
    }

    suspend fun setRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AuthPreferencesKeys.refreshToken] = token
        }
    }

    suspend fun setLogoutNotice(notice: String) {
        dataStore.edit { preferences ->
            preferences[AuthPreferencesKeys.logoutNotice] = notice
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferencesKeys.accessToken)
        }
    }

    suspend fun clearRefreshToken() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferencesKeys.refreshToken)
        }
    }

    suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferencesKeys.accessToken)
            preferences.remove(AuthPreferencesKeys.refreshToken)
        }
    }

    suspend fun clearLogoutNotice() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferencesKeys.logoutNotice)
        }
    }

    suspend fun consumeLogoutNotice(): String? {
        val notice = getLogoutNotice()
        if (notice != null) {
            clearLogoutNotice()
        }
        return notice
    }
}

