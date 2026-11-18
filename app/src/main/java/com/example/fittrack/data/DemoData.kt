package com.example.fittrack.data

import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.model.WorkoutPlan

object DemoData {

    val exercises = listOf(
        Exercise("db-bench", "Dumbbell Bench Press", "Chest"),
        Exercise("shoulder-press", "Seated Dumbbell Press", "Shoulders"),
        Exercise("triceps", "Triceps Extension", "Triceps"),

        Exercise("pull-ups", "Pull-ups", "Back"),
        Exercise("seated-row", "Seated Cable Row", "Back"),
        Exercise("biceps", "Biceps Curl", "Biceps"),

        Exercise("squat", "Squats", "Legs"),
        Exercise("leg-extension", "Leg Extension", "Legs"),
        Exercise("leg-curl", "Leg Curl", "Legs")
    )

    val plans = listOf(
        WorkoutPlan(
            id = "push-day",
            name = "Push Day",
            dayOfWeek = 1,
            exercises = listOf(
                PlannedExercise("db-bench", 3, 12, 15.0),
                PlannedExercise("shoulder-press", 3, 10, 12.0),
                PlannedExercise("triceps", 3, 12, 10.0)
            )
        ),

        WorkoutPlan(
            id = "pull-day",
            name = "Pull Day",
            dayOfWeek = 3,
            exercises = listOf(
                PlannedExercise("pull-ups", 3, 8, 0.0),
                PlannedExercise("seated-row", 3, 12, 25.0),
                PlannedExercise("biceps", 3, 12, 10.0)
            )
        ),

        WorkoutPlan(
            id = "leg-day",
            name = "Leg Day",
            dayOfWeek = 5,
            exercises = listOf(
                PlannedExercise("squat", 4, 10, 40.0),
                PlannedExercise("leg-extension", 3, 12, 25.0),
                PlannedExercise("leg-curl", 3, 12, 20.0)
            )
        )
    )
}