package com.sevvanam.android_interview_architect.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
        val PROFILE_BIO = stringPreferencesKey("profile_bio")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    val lastSyncTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] ?: "Never"
        }

    // Defaults match the profile values that were previously hardcoded directly in ProfileViewModel,
    // so wiring persistence in doesn't change first-run UX.
    val bio: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PROFILE_BIO] ?: "Staff Android Engineer & Tech Interview Coach"
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setLastSyncTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = time
        }
    }

    suspend fun setBio(bio: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROFILE_BIO] = bio
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
