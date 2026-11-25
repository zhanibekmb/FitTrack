package com.example.fittrack.ui.home

import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession

internal fun dashboardState(
    plans: List<WorkoutPlan>,
    sessions: List<WorkoutSession>,
    activeSession: WorkoutSession?,
    today: Int
): HomeUiState {
    val finished = sessions.filter { it.isFinished }
    return HomeUiState(
        totalPlans = plans.size,
        completedWorkouts = finished.size,
        totalVolumeKg = finished.sumOf { it.totalVolumeKg() },
        todayPlans = plans.filter { it.dayOfWeek == today },
        activePlanName = plans.find { it.id == activeSession?.planId }?.name,
        activePlanId = activeSession?.planId
    )
}
