package com.dlight.data.utils

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrefsConstants {
    const val TASKS_SYNC_PREFS = "task_sync_preferences"
    val KEY_USER_ID = stringPreferencesKey("user_id")
    val KEY_EMAIL = stringPreferencesKey("email")
    val KEY_TOKEN = stringPreferencesKey("token")
    val KEY_LAST_SYNC = longPreferencesKey("last_sync_time")
}