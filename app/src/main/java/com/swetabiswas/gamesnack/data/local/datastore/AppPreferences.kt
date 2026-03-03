package com.swetabiswas.gamesnack.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

data class AppPreferenceState(
    val isDarkMode: Boolean   = true,
    val isSoundEnabled: Boolean = true
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE     = booleanPreferencesKey("dark_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val appPreferenceState: Flow<AppPreferenceState> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppPreferenceState(
                isDarkMode     = prefs[Keys.DARK_MODE]     ?: true,
                isSoundEnabled = prefs[Keys.SOUND_ENABLED] ?: true
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }
}
