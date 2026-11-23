package com.example.fittrack.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fittrack.data.model.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "fittrack_settings")
private val themeKey = stringPreferencesKey("theme_mode")

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences ->
            val savedTheme = preferences[themeKey]
            ThemeMode.entries.find { it.name == savedTheme } ?: ThemeMode.SYSTEM
        }
        .retryWhen { error, _ ->
            if (error !is IOException) return@retryWhen false
            // Let the app open with the system theme while a read is retried.
            emit(ThemeMode.SYSTEM)
            delay(1_000)
            true
        }

    suspend fun saveTheme(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeKey] = mode.name
        }
    }
}
