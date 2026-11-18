package com.example.fittrack.ui.workout

import com.example.fittrack.data.model.CompletedSet
import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.model.WorkoutPlan

internal fun parsedWeight(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()
    ?.takeIf { it.isFinite() && it >= 0 }

internal fun weightInput(value: Double): String = value.toString().removeSuffix(".0")

// Repeated entries share one card, with distinct set numbers and their own targets.
internal fun workoutExercises(
    plan: WorkoutPlan,
    library: List<Exercise>,
    completed: List<CompletedSet>
): List<WorkoutExerciseUiState> = plan.exercises.groupBy { it.exerciseId }.map { (id, entries) ->
    val exercise = library.find { it.id == id }
    val targets = entries.flatMap { entry ->
        List(entry.targetSets) { entry }
    }
    val savedSets = completed.filter { it.exerciseId == id }
    val count = maxOf(targets.size, savedSets.maxOfOrNull { it.setNumber } ?: 0)
    WorkoutExerciseUiState(
        id = id,
        name = exercise?.name ?: id,
        muscleGroup = exercise?.muscleGroup.orEmpty(),
        sets = (1..count).map { number ->
            val saved = savedSets.find { it.setNumber == number }
            val target = targets.getOrNull(number - 1) ?: targets.last()
            WorkoutSetUiState(
                number = number,
                weight = weightInput(saved?.weightKg ?: target.targetWeightKg),
                reps = (saved?.reps ?: target.targetReps).toString(),
                completed = saved != null
            )
        }
    )
}
