package com.example.fittrack.data.repository

import com.example.fittrack.data.DemoData
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.CompletedSet
import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class WorkoutData(
    val plans: List<WorkoutPlan> = DemoData.plans,
    val exercises: List<Exercise> = DemoData.exercises,
    val sessions: List<WorkoutSession> = emptyList(),
    val activeSession: WorkoutSession? = null
)

// Screen actions call these methods on the main thread.
class WorkoutRepository {
    private val _data = MutableStateFlow(WorkoutData())
    val data: StateFlow<WorkoutData> = _data.asStateFlow()

    fun createPlan(name: String, dayOfWeek: Int): WorkoutPlan {
        require(name.isNotBlank()) { "Plan name cannot be empty" }
        require(dayOfWeek in 1..7) { "Day must be between 1 and 7" }

        val plan = WorkoutPlan(UUID.randomUUID().toString(), name.trim(), dayOfWeek)
        val current = _data.value
        _data.value = current.copy(plans = current.plans + plan)
        return plan
    }

    fun editPlan(planId: String, name: String, dayOfWeek: Int): Boolean {
        if (name.isBlank() || dayOfWeek !in 1..7) return false
        val plan = _data.value.plans.find { it.id == planId } ?: return false
        replacePlan(plan.copy(name = name.trim(), dayOfWeek = dayOfWeek))
        return true
    }

    fun deletePlan(planId: String): Boolean {
        val current = _data.value
        if (current.activeSession?.planId == planId) return false
        if (current.plans.none { it.id == planId }) return false

        _data.value = current.copy(plans = current.plans.filter { it.id != planId })
        return true
    }

    fun addExerciseToPlan(
        planId: String,
        exerciseId: String,
        targetSets: Int,
        targetReps: Int,
        targetWeightKg: Double
    ): Boolean = addExerciseToPlan(planId, exerciseId, targetSets, targetReps, targetWeightKg, null)

    fun addCatalogExerciseToPlan(
        planId: String,
        exercise: CatalogExercise,
        targetSets: Int,
        targetReps: Int,
        targetWeightKg: Double
    ): Boolean {
        if (exercise.wgerId <= 0 || exercise.name.isBlank()) return false
        return addExerciseToPlan(
            planId, exercise.id, targetSets, targetReps, targetWeightKg, exercise.toWorkoutExercise()
        )
    }

    private fun addExerciseToPlan(
        planId: String,
        exerciseId: String,
        targetSets: Int,
        targetReps: Int,
        targetWeightKg: Double,
        imported: Exercise?
    ): Boolean {
        if (targetSets <= 0 || targetReps <= 0) return false
        if (!targetWeightKg.isFinite() || targetWeightKg < 0) return false

        val current = _data.value
        val plan = current.plans.find { it.id == planId } ?: return false
        val exercises = if (current.exercises.any { it.id == exerciseId }) {
            current.exercises
        } else {
            current.exercises + (imported ?: return false)
        }
        val planned = PlannedExercise(exerciseId, targetSets, targetReps, targetWeightKg)
        replacePlan(plan.copy(exercises = plan.exercises + planned), current.copy(exercises = exercises))
        return true
    }

    private fun replacePlan(updatedPlan: WorkoutPlan, current: WorkoutData = _data.value) {
        val plans = current.plans.toMutableList()
        val index = plans.indexOfFirst { it.id == updatedPlan.id }
        plans[index] = updatedPlan
        _data.value = current.copy(plans = plans.toList())
    }

    fun startWorkout(planId: String): WorkoutSession? {
        val current = _data.value
        if (current.activeSession != null) return null
        val plan = current.plans.find { it.id == planId } ?: return null
        if (plan.exercises.isEmpty()) return null

        val session = WorkoutSession(
            id = UUID.randomUUID().toString(),
            planId = plan.id,
            startedAt = System.currentTimeMillis()
        )
        _data.value = current.copy(activeSession = session)
        return session
    }

    fun completeSet(exerciseId: String, setNumber: Int, weightKg: Double, reps: Int): Boolean {
        if (setNumber <= 0 || reps <= 0) return false
        if (!weightKg.isFinite() || weightKg < 0) return false

        val current = _data.value
        val session = current.activeSession ?: return false
        val plan = current.plans.find { it.id == session.planId } ?: return false
        if (plan.exercises.none { it.exerciseId == exerciseId }) return false

        for (set in session.completedSets) {
            if (set.exerciseId == exerciseId && set.setNumber == setNumber) return false
        }
        val newSet = CompletedSet(exerciseId, setNumber, weightKg, reps)
        val updatedSession = session.copy(completedSets = session.completedSets + newSet)
        _data.value = current.copy(activeSession = updatedSession)
        return true
    }

    fun removeCompletedSet(exerciseId: String, setNumber: Int): Boolean {
        val current = _data.value
        val session = current.activeSession ?: return false
        val remaining = session.completedSets.filterNot {
            it.exerciseId == exerciseId && it.setNumber == setNumber
        }
        if (remaining.size == session.completedSets.size) return false
        _data.value = current.copy(activeSession = session.copy(completedSets = remaining))
        return true
    }

    fun discardWorkout(): Boolean {
        val current = _data.value
        if (current.activeSession == null) return false
        _data.value = current.copy(activeSession = null)
        return true
    }

    fun finishWorkout(): WorkoutSession? {
        val current = _data.value
        val session = current.activeSession ?: return null
        if (session.completedSets.isEmpty()) return null

        val finishedSession = session.copy(
            finishedAt = System.currentTimeMillis(),
            planName = current.plans.find { it.id == session.planId }?.name
        )
        _data.value = current.copy(
            sessions = current.sessions + finishedSession,
            activeSession = null
        )
        return finishedSession
    }

    fun getPreviousSets(exerciseId: String): List<CompletedSet> {
        val sessions = _data.value.sessions
        for (index in sessions.indices.reversed()) {
            val sets = sessions[index].completedSets.filter { it.exerciseId == exerciseId }
            if (sets.isNotEmpty()) return sets.sortedBy { it.setNumber }
        }
        return emptyList()
    }
}
