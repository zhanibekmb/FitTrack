package com.example.fittrack.data.model

data class WorkoutPlan(
    val id: String,
    val name: String,
    val dayOfWeek: Int,
    val exercises: List<PlannedExercise> = emptyList()
)
