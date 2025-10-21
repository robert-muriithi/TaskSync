package com.dlight.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.dlight.data.utils.PrefsConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PrefsConstants.TASKS_SYNC_PREFS
)

class UserPreferences(
    context: Context
) {
    private val dataStore = context.dataStore

    suspend fun saveUser(userId: String, email: String, token: String) {
        dataStore.edit { preferences ->
            preferences[PrefsConstants.KEY_USER_ID] = userId
            preferences[PrefsConstants.KEY_EMAIL] = email
            preferences[PrefsConstants.KEY_TOKEN] = token
        }
    }

    suspend fun getUserId(): String? {
        return dataStore.data.map { preferences ->
            preferences[PrefsConstants.KEY_USER_ID]
        }.first()
    }

    suspend fun getEmail(): String? {
        return dataStore.data.map { preferences ->
            preferences[PrefsConstants.KEY_EMAIL]
        }.first()
    }

    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[PrefsConstants.KEY_TOKEN]
        }.first()
    }

    fun observeToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PrefsConstants.KEY_TOKEN]
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    fun observeIsLoggedIn(): Flow<Boolean> {
        return observeToken().map { token ->
            !token.isNullOrEmpty()
        }
    }

    suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getLastSyncTime(): Long {
        return dataStore.data.map { preferences ->
            preferences[PrefsConstants.KEY_LAST_SYNC] ?: 0L
        }.first()
    }

    suspend fun updateLastSyncTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PrefsConstants.KEY_LAST_SYNC] = timestamp
        }
    }

    fun observeUserData(): Flow<UserData> {
        return dataStore.data.map { preferences ->
            UserData(
                userId = preferences[PrefsConstants.KEY_USER_ID],
                email = preferences[PrefsConstants.KEY_EMAIL],
                token = preferences[PrefsConstants.KEY_TOKEN],
                lastSyncTime = preferences[PrefsConstants.KEY_LAST_SYNC] ?: 0L
            )
        }
    }

}

