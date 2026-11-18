package com.example.fittrack.ui.home

import com.example.fittrack.data.model.WorkoutPlan

data class HomeUiState(
    val totalPlans: Int = 0,
    val completedWorkouts: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val todayPlans: List<WorkoutPlan> = emptyList(),
    val activePlanName: String? = null
)