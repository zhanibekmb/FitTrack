package com.example.fittrack.ui.settings

import com.example.fittrack.data.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
    val saveFailed: Boolean = false
)
