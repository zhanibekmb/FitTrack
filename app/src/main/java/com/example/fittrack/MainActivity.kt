package com.example.fittrack

import android.os.Bundle
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fittrack.navigation.FitTrackNavGraph
import com.example.fittrack.ui.theme.FitTrackTheme

class MainActivity : ComponentActivity()     {
    override fun attachBaseContext(newBase: Context) {
        // Keep app and Material component resources in English on every device language.
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocales(LocaleList(Locale.ENGLISH))
        configuration.setLayoutDirection(Locale.ENGLISH)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackTheme { FitTrackNavGraph() }
        }
    }
}
