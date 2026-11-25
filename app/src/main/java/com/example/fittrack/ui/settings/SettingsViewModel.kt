package com.example.fittrack.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.model.ThemeMode
import com.example.fittrack.data.repository.FitTrackContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FitTrackContainer.settingsRepository(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode, isLoading = false)
            }
        }
    }

    fun selectTheme(mode: ThemeMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveFailed = false)
            try {
                repository.saveTheme(mode)
            } catch (_: IOException) {
                _uiState.value = _uiState.value.copy(saveFailed = true)
            }
        }
    }
}
