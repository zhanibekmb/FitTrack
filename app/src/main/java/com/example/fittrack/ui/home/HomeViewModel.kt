
package com.example.fittrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(
    repository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.plans,
        repository.sessions,
        repository.activeSession
    ) { plans, sessions, activeSession ->

        // Today's day: Monday = 1, Sunday = 7
        val today = LocalDate.now().dayOfWeek.value

        // Total training volume
        val totalVolume = sessions.sumOf { session ->
            session.completedSets.sumOf { set ->
                set.volumeKg
            }
        }

        // Find today's scheduled plans
        val scheduledPlans = plans.filter { plan ->
            plan.dayOfWeek == today
        }

        // Find active workout name
        val activeName = activeSession?.let { session ->
            plans.find { plan ->
                plan.id == session.planId
            }?.name ?: "Workout"
        }

        HomeUiState(
            totalPlans = plans.size,
            completedWorkouts = sessions.count {
                it.isFinished
            },
            totalVolumeKg = totalVolume,
            todayPlans = scheduledPlans,
            activePlanName = activeName
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
