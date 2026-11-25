package com.example.fittrack.ui.plans

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.repository.FitTrackContainer

class PlansViewModel : ViewModel() {
    private val repository = FitTrackContainer.repository
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
}
