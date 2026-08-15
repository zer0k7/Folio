package com.ghost.folio.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "folio_settings")

enum class ThemeSetting(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromValue(value: String?): ThemeSetting {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

enum class UpdateFrequency(val days: Int, val labelResId: Int) {
    DAILY(1, com.ghost.folio.R.string.freq_daily),
    EVERY_3_DAYS(3, com.ghost.folio.R.string.freq_3_days),
    EVERY_7_DAYS(7, com.ghost.folio.R.string.freq_7_days),
    NEVER(-1, com.ghost.folio.R.string.freq_never);

    companion object {
        fun fromDays(days: Int?): UpdateFrequency {
            return entries.find { it.days == days } ?: DAILY
        }
    }
}

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UPDATE_FREQUENCY = intPreferencesKey("update_frequency_days")
        val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check_time")
        val LAST_SEEN_VERSION_CODE = intPreferencesKey("last_seen_version_code")
        val FONT_SCALE = androidx.datastore.preferences.core.floatPreferencesKey("font_scale")
    }

    val themeSetting: Flow<ThemeSetting> = context.dataStore.data.map { preferences ->
        ThemeSetting.fromValue(preferences[Keys.THEME_MODE])
    }

    val updateFrequency: Flow<UpdateFrequency> = context.dataStore.data.map { preferences ->
        UpdateFrequency.fromDays(preferences[Keys.UPDATE_FREQUENCY])
    }

    val lastUpdateCheckTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.LAST_UPDATE_CHECK] ?: 0L
    }

    val lastSeenVersionCode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.LAST_SEEN_VERSION_CODE] ?: 0
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[Keys.FONT_SCALE] ?: 1.0f
    }

    suspend fun setThemeSetting(theme: ThemeSetting) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = theme.value
        }
    }

    suspend fun setUpdateFrequency(frequency: UpdateFrequency) {
        context.dataStore.edit { preferences ->
            preferences[Keys.UPDATE_FREQUENCY] = frequency.days
        }
    }

    suspend fun setLastUpdateCheckTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_UPDATE_CHECK] = timestamp
        }
    }

    suspend fun setLastSeenVersionCode(versionCode: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_SEEN_VERSION_CODE] = versionCode
        }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[Keys.FONT_SCALE] = scale
        }
    }
}
