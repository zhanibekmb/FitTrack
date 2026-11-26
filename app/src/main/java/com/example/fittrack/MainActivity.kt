package com.example.fittrack

import android.os.Bundle
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.data.model.ThemeMode
import com.example.fittrack.ui.settings.SettingsViewModel
import com.example.fittrack.navigation.FitTrackNavGraph
import com.example.fittrack.ui.theme.FitTrackTheme
import com.example.fittrack.data.repository.FitTrackContainer

class MainActivity : ComponentActivity()     {
    override fun attachBaseContext(newBase: Context) {
        // Keep app and Material component resources in English on every device language.
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocales(LocaleList(Locale.ENGLISH))
        configuration.setLayoutDirection(Locale.ENGLISH)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FitTrackContainer.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()
            if (!settings.isLoading) {
                val darkTheme = when (settings.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
                SideEffect {
                    val systemBars = WindowCompat.getInsetsController(window, window.decorView)
                    systemBars.isAppearanceLightStatusBars = !darkTheme
                    systemBars.isAppearanceLightNavigationBars = !darkTheme
                }
                FitTrackTheme(darkTheme = darkTheme) {
                    FitTrackNavGraph(settings, settingsViewModel::selectTheme)
                }
            }
        }
    }
}
