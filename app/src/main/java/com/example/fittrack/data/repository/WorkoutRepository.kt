package com.example.fittrack.data.repository

import com.example.fittrack.data.DemoData
import com.example.fittrack.data.model.CompletedSet
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

    fun startWorkout(planId: String): WorkoutSession? {

        // Check if another workout is active
        if (_activeSession.value != null) {
            return null
        }

        // Find selected plan
        val plan = _plans.value.find {
            it.id == planId
        } ?: return null

        // Create new workout session
        val newSession = WorkoutSession(
            id = UUID.randomUUID().toString(),
            planId = plan.id,
            startedAt = System.currentTimeMillis()
        )

        // Update active workout
        _activeSession.value = newSession

        return newSession
    }

    fun completeSet(
        exerciseId: String,
        setNumber: Int,
        weightKg: Double,
        reps: Int
    ): Boolean {

        // Check if workout is active
        val session = _activeSession.value
            ?: return false

        // Validate input
        if (setNumber <= 0 || reps <= 0) {
            return false
        }

        if (!weightKg.isFinite() || weightKg < 0) {
            return false
        }

        // Check exercise belongs to current plan
        val plan = _plans.value.find {
            it.id == session.planId
        } ?: return false

        val exerciseExists = plan.exercises.any {
            it.exerciseId == exerciseId
        }

        if (!exerciseExists) {
            return false
        }

        // Avoid duplicate set numbers
        val alreadyCompleted = session.completedSets.any {
            it.exerciseId == exerciseId &&
                    it.setNumber == setNumber
        }

        if (alreadyCompleted) {
            return false
        }

        // Create completed set
        val completedSet = CompletedSet(
            exerciseId = exerciseId,
            setNumber = setNumber,
            weightKg = weightKg,
            reps = reps
        )

        // Update active session
        _activeSession.value = session.copy(
            completedSets =
                session.completedSets + completedSet
        )

        return true
    }

    fun removeCompletedSet(exerciseId: String, setNumber: Int): Boolean {
        val session = _activeSession.value ?: return false
        val remaining = session.completedSets.filterNot {
            it.exerciseId == exerciseId && it.setNumber == setNumber
        }
        if (remaining.size == session.completedSets.size) return false
        _activeSession.value = session.copy(completedSets = remaining)
        return true
    }

    fun finishWorkout(): WorkoutSession? {

        val session = _activeSession.value
            ?: return null

        // Do not save empty workouts
        if (session.completedSets.isEmpty()) {
            return null
        }

        val finishedSession = session.copy(
            finishedAt = System.currentTimeMillis()
        )

        // Add workout to history
        _sessions.update { currentSessions ->
            currentSessions + finishedSession
        }

        // Clear active workout
        _activeSession.value = null

        return finishedSession
    }


    fun getPreviousSets(
        exerciseId: String
    ): List<CompletedSet> {

        // Find latest completed session with exercise
        val previousSession = _sessions.value
            .asReversed()
            .firstOrNull { session ->
                session.completedSets.any {
                    it.exerciseId == exerciseId
                }
            }

        // Return completed sets for this exercise
        return previousSession
            ?.completedSets
            ?.filter {
                it.exerciseId == exerciseId
            }
            ?.sortedBy {
                it.setNumber
            }
            .orEmpty()
    }

}