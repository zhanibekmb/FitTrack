package com.example.fittrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class HomeViewModel(repository: WorkoutRepository) : ViewModel() {
    // Refresh the local date while observed, including after returning from background.
    private val currentDay = flow {
        while (true) {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            emit((day + 5) % 7 + 1) // Monday = 1; compatible with API 24.
            delay(30_000)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.plans, repository.sessions, repository.activeSession, currentDay
    ) { plans, sessions, activeSession, today ->
        dashboardState(plans, sessions, activeSession, today)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
