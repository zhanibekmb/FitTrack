package com.example.fittrack.ui.workout

import com.example.fittrack.data.model.CompletedSet

data class WorkoutSetUiState(
    val number: Int,
    val weight: String,
    val reps: String,
    val completed: Boolean = false,
    val showErrors: Boolean = false,
    val previous: CompletedSet? = null
)

data class WorkoutExerciseUiState(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val sets: List<WorkoutSetUiState>
)

enum class WorkoutMessage { INVALID_INPUT, SAVE_FAILED, EMPTY_WORKOUT, SESSION_UNAVAILABLE }

data class WorkoutUiState(
    val sessionId: String? = null,
    val planId: String? = null,
    val planName: String = "",
    val exercises: List<WorkoutExerciseUiState> = emptyList(),
    val message: WorkoutMessage? = null
) {
    val completedSets: Int get() = exercises.sumOf { exercise -> exercise.sets.count { it.completed } }
    val totalSets: Int get() = exercises.sumOf { it.sets.size }
}
