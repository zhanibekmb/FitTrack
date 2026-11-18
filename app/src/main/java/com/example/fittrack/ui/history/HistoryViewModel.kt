package com.example.fittrack.ui.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WorkoutSession(
    val id: String,
    val date: String,
    val durationMinutes: Int,
    val exercises: List<ExerciseSet>
)

data class ExerciseSet(
    val exerciseId: String,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val completed: Boolean
)

class HistoryViewModel : ViewModel() {
    private val _sessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val sessions: StateFlow<List<WorkoutSession>> = _sessions.asStateFlow()

    init {
        // Моковые данные для стабильной работы и отображения графиков
        _sessions.value = listOf(
            WorkoutSession(
                id = "1",
                date = "12 октября 2026",
                durationMinutes = 45,
                exercises = listOf(
                    ExerciseSet("ex_bench", "Жим лежа", 60.0, 10, true),
                    ExerciseSet("ex_bench", "Жим лежа", 65.0, 8, true)
                )
            ),
            WorkoutSession(
                id = "2",
                date = "15 октября 2026",
                durationMinutes = 50,
                exercises = listOf(
                    ExerciseSet("ex_bench", "Жим лежа", 70.0, 6, true)
                )
            )
        )
    }
}