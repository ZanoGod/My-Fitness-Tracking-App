package com.mad.myfitnesstrackingapp.notifications

import android.R.attr.x
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFS_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

object NotificationPrefs {
    private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

    fun notificationsEnabledFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] ?: false // default ON
        }
    }

    suspend fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->   // inferred type is MutablePreferences ✅
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
