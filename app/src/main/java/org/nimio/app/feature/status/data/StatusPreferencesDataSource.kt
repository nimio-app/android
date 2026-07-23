package org.nimio.app.feature.status.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.nimio.app.feature.status.domain.Availability
import org.nimio.app.feature.status.domain.UserStatus
import java.io.IOException

private const val STATUS_DATASTORE_NAME = "nimio_status"

private val Context.statusDataStore: DataStore<Preferences> by preferencesDataStore(name = STATUS_DATASTORE_NAME)

private object StatusPreferencesKeys {
    val availability = stringPreferencesKey("availability")
    val activity = stringPreferencesKey("activity")
    val updatedAt = longPreferencesKey("updated_at")
    val note = stringPreferencesKey("note")
    val expiresAt = longPreferencesKey("expires_at")
}

class StatusPreferencesDataSource(
    context: Context
) {
    private val dataStore: DataStore<Preferences> = context.statusDataStore

    fun observeStatus(): Flow<UserStatus> {
        return dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                val availabilityValue = preferences[StatusPreferencesKeys.availability]
                UserStatus(
                    availability = availabilityValue
                        ?.let { value -> runCatching { Availability.valueOf(value) }.getOrNull() }
                        ?: Availability.FREE,
                    activity = preferences[StatusPreferencesKeys.activity].orEmpty(),
                    note = preferences[StatusPreferencesKeys.note].orEmpty(),
                    updatedAtEpochMillis = preferences[StatusPreferencesKeys.updatedAt],
                    expiresAtEpochMillis = preferences[StatusPreferencesKeys.expiresAt]
                )
            }
    }

    suspend fun saveStatus(status: UserStatus) {
        dataStore.edit { preferences ->
            preferences[StatusPreferencesKeys.availability] = status.availability.name
            preferences[StatusPreferencesKeys.activity] = status.activity
            status.updatedAtEpochMillis?.let {
                preferences[StatusPreferencesKeys.updatedAt] = it
            }
            preferences[StatusPreferencesKeys.note] = status.note
            if (status.expiresAtEpochMillis != null) {
                preferences[StatusPreferencesKeys.expiresAt] = status.expiresAtEpochMillis
            } else {
                preferences.remove(StatusPreferencesKeys.expiresAt)
            }
        }
    }
}


