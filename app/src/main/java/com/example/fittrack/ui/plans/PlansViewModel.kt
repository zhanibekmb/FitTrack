package com.example.fittrack.ui.plans

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.repository.WorkoutRepository

class PlansViewModel(private val repository: WorkoutRepository) : ViewModel() {
    val plans = repository.plans
    val exercises = repository.exercises

    fun createPlan(name: String, dayOfWeek: Int) = repository.createPlan(name, dayOfWeek)

    fun addExercise(planId: String, exerciseId: String, sets: Int, reps: Int, weightKg: Double): Boolean =
        repository.addExerciseToPlan(planId, exerciseId, sets, reps, weightKg)
}
