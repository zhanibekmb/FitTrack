package com.example.fittrack.data.repository

import com.example.fittrack.data.DemoData
import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class WorkoutRepository {
    // Exercise library
    private val _exercises = MutableStateFlow(
        DemoData.exercises.toList()
    )
    val exercises: StateFlow<List<Exercise>> =
        _exercises.asStateFlow()

    // Workout plans
    private val _plans = MutableStateFlow(
        DemoData.plans.toList()
    )

    val plans: StateFlow<List<WorkoutPlan>> =
        _plans.asStateFlow()

    // Completed workout history
    private val _sessions =
        MutableStateFlow<List<WorkoutSession>>(emptyList())

    val sessions: StateFlow<List<WorkoutSession>> =
        _sessions.asStateFlow()

    // Current active workout
    private val _activeSession =
        MutableStateFlow<WorkoutSession?>(null)

    val activeSession: StateFlow<WorkoutSession?> =
        _activeSession.asStateFlow()

    // Create workout plan
    fun createPlan(
        name: String,
        dayOfWeek: Int
    ): WorkoutPlan {

        require(name.isNotBlank()) {
            "Plan name cannot be empty"
        }

        require(dayOfWeek in 1..7) {
            "Day must be between 1 and 7"
        }

        val newPlan = WorkoutPlan(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            dayOfWeek = dayOfWeek
        )

        _plans.update { currentPlans ->
            currentPlans + newPlan
        }

        return newPlan
    }

    // Add exercise to existing plan
    fun addExerciseToPlan(
        planId: String,
        exerciseId: String,
        targetSets: Int,
        targetReps: Int,
        targetWeightKg: Double
    ): Boolean {

        if (targetSets <= 0 || targetReps <= 0) {
            return false
        }

        if (!targetWeightKg.isFinite() || targetWeightKg < 0) {
            return false
        }

        val exerciseExists = _exercises.value.any {
            it.id == exerciseId
        }

        val planExists = _plans.value.any {
            it.id == planId
        }

        if (!exerciseExists || !planExists) {
            return false
        }

        val plannedExercise = PlannedExercise(
            exerciseId = exerciseId,
            targetSets = targetSets,
            targetReps = targetReps,
            targetWeightKg = targetWeightKg
        )

        _plans.update { currentPlans ->
            currentPlans.map { plan ->

                if (plan.id == planId) {
                    plan.copy(
                        exercises = plan.exercises + plannedExercise
                    )
                } else {
                    plan
                }
            }
        }

        return true
    }
}