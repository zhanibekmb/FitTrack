package com.example.fittrack.ui.plans

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.data.repository.WorkoutRepository

class PlansViewModel(
    private val repository: WorkoutRepository = FitTrackContainer.repository
) : ViewModel() {
    val data = repository.data

    fun createPlan(name: String, dayOfWeek: Int): WorkoutPlan {
        return repository.createPlan(name, dayOfWeek)
    }

    fun editPlan(planId: String, name: String, dayOfWeek: Int): Boolean {
        return repository.editPlan(planId, name, dayOfWeek)
    }

    fun deletePlan(planId: String): Boolean {
        return repository.deletePlan(planId)
    }

    fun addExercise(planId: String, exerciseId: String, sets: Int, reps: Int, weightKg: Double): Boolean {
        return repository.addExerciseToPlan(planId, exerciseId, sets, reps, weightKg)
    }

    fun addCatalogExercise(
        planId: String,
        exercise: CatalogExercise,
        sets: Int,
        reps: Int,
        weightKg: Double
    ): Boolean = repository.addCatalogExerciseToPlan(planId, exercise, sets, reps, weightKg)
}
