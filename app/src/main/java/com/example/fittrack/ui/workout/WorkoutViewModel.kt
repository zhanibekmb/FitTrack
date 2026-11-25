package com.example.fittrack.ui.workout

import androidx.lifecycle.ViewModel
import com.example.fittrack.data.repository.FitTrackContainer

enum class WorkoutStartResult {
    READY, INVALID_PLAN, EMPTY_PLAN, OTHER_WORKOUT_ACTIVE
}

class WorkoutViewModel : ViewModel() {
    private val repository = FitTrackContainer.repository
    val data = repository.data

    fun startOrResume(planId: String): WorkoutStartResult {
        val current = data.value
        val plan = current.plans.find { it.id == planId } ?: return WorkoutStartResult.INVALID_PLAN
        val active = current.activeSession
        if (active != null) {
            if (active.planId == planId) return WorkoutStartResult.READY
            return WorkoutStartResult.OTHER_WORKOUT_ACTIVE
        }
        if (plan.exercises.isEmpty()) return WorkoutStartResult.EMPTY_PLAN
        if (repository.startWorkout(planId) == null) return WorkoutStartResult.INVALID_PLAN
        return WorkoutStartResult.READY
    }

    fun recordSet(planId: String, exerciseId: String, number: Int, weight: Double, reps: Int): Boolean {
        if (data.value.activeSession?.planId != planId) return false
        return repository.completeSet(exerciseId, number, weight, reps)
    }

    fun finishWorkout(planId: String): Boolean {
        if (data.value.activeSession?.planId != planId) return false
        return repository.finishWorkout() != null
    }
}
