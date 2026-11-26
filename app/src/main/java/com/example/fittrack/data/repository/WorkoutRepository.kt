package com.example.fittrack.data.repository

import com.example.fittrack.data.DemoData
import com.example.fittrack.data.model.CompletedSet
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class WorkoutData(
    val plans: List<WorkoutPlan> = DemoData.plans,
    val exercises: List<Exercise> = DemoData.exercises,
    val sessions: List<WorkoutSession> = emptyList(),
    val activeSession: WorkoutSession? = null,
    val isLoading: Boolean = false,
    val storageError: Boolean = false,
    val isSavingWorkout: Boolean = false
)

// Active workout input stays in memory. A successful persistent action has already committed to Room.
class WorkoutRepository(private val storage: WorkoutStorage? = null) {
    private val writes = Mutex()
    private var changingPlan = false
    private val _data = MutableStateFlow(WorkoutData(
        plans = if (storage == null) DemoData.plans else emptyList(),
        isLoading = storage != null
    ))
    val data: StateFlow<WorkoutData> = _data.asStateFlow()

    suspend fun observeStorage() {
        val local = storage ?: return
        _data.value = _data.value.copy(isLoading = true, storageError = false)
        try {
            writes.withLock { local.initialize(DemoData.plans) }
            local.changes.collect {
                // Re-read under the write lock so an older Flow emission cannot undo a completed action.
                writes.withLock { refreshStoredData() }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _data.value = _data.value.copy(isLoading = false, storageError = true)
        }
    }

    private suspend fun refreshStoredData() {
        val saved = storage?.read() ?: return
        _data.value = _data.value.copy(plans = saved.plans, sessions = saved.sessions, exercises = saved.exercises,
            isLoading = false, storageError = false)
    }

    private suspend fun <T> changePlan(action: suspend () -> T): T = writes.withLock {
        changingPlan = true
        try {
            withContext(NonCancellable) { action() }
        } finally {
            changingPlan = false
        }
    }

    suspend fun createPlan(name: String, dayOfWeek: Int): WorkoutPlan = changePlan {
        check(!_data.value.isLoading && !_data.value.storageError) { "Database is unavailable" }
        require(name.isNotBlank()) { "Plan name cannot be empty" }
        require(dayOfWeek in 1..7) { "Day must be between 1 and 7" }

        val plan = WorkoutPlan(UUID.randomUUID().toString(), name.trim(), dayOfWeek)
        storage?.savePlan(plan)
        val current = _data.value
        _data.value = current.copy(plans = current.plans + plan)
        plan
    }

    suspend fun editPlan(planId: String, name: String, dayOfWeek: Int): Boolean = changePlan {
        if (!canEdit(planId) || name.isBlank() || dayOfWeek !in 1..7) return@changePlan false
        val plan = _data.value.plans.find { it.id == planId } ?: return@changePlan false
        replacePlan(plan.copy(name = name.trim(), dayOfWeek = dayOfWeek))
        true
    }

    suspend fun deletePlan(planId: String): Boolean = changePlan {
        if (!canEdit(planId) || _data.value.plans.none { it.id == planId }) return@changePlan false
        storage?.deletePlan(planId)
        val current = _data.value
        _data.value = current.copy(plans = current.plans.filter { it.id != planId })
        true
    }

    suspend fun addExerciseToPlan(
        planId: String,
        exerciseId: String,
        targetSets: Int,
        targetReps: Int,
        targetWeightKg: Double
    ): Boolean = changePlan {
        if (!canEdit(planId) || !validTargets(exerciseId, targetSets, targetReps, targetWeightKg)) return@changePlan false
        val plan = _data.value.plans.find { it.id == planId } ?: return@changePlan false
        val exercise = PlannedExercise(exerciseId, targetSets, targetReps, targetWeightKg)
        replacePlan(plan.copy(exercises = plan.exercises + exercise))
        true
    }

    suspend fun editPlannedExercise(planId: String, index: Int, exercise: PlannedExercise): Boolean = changePlan {
        if (!canEdit(planId) || !validTargets(exercise.exerciseId, exercise.targetSets,
                exercise.targetReps, exercise.targetWeightKg)) return@changePlan false
        val plan = _data.value.plans.find { it.id == planId } ?: return@changePlan false
        if (index !in plan.exercises.indices) return@changePlan false
        replacePlan(plan.copy(exercises = plan.exercises.toMutableList().apply { set(index, exercise) }))
        true
    }

    suspend fun addCatalogExerciseToPlan(planId: String, exercise: CatalogExercise,
                                        targetSets: Int, targetReps: Int, targetWeightKg: Double): Boolean = changePlan {
        if (!canEdit(planId) || exercise.wgerId <= 0 || exercise.name.isBlank()) return@changePlan false
        if (targetSets <= 0 || targetReps <= 0 || !targetWeightKg.isFinite() || targetWeightKg < 0) return@changePlan false
        val plan = _data.value.plans.find { it.id == planId } ?: return@changePlan false
        val imported = exercise.toWorkoutExercise()
        val updated = plan.copy(exercises = plan.exercises +
            PlannedExercise(imported.id, targetSets, targetReps, targetWeightKg))
        replacePlan(updated, imported)
        true
    }

    suspend fun removePlannedExercise(planId: String, index: Int): Boolean = changePlan {
        if (!canEdit(planId)) return@changePlan false
        val plan = _data.value.plans.find { it.id == planId } ?: return@changePlan false
        if (index !in plan.exercises.indices) return@changePlan false
        replacePlan(plan.copy(exercises = plan.exercises.filterIndexed { position, _ -> position != index }))
        true
    }

    private fun canEdit(planId: String) = !_data.value.isLoading && !_data.value.storageError &&
        _data.value.activeSession?.planId != planId

    private fun validTargets(exerciseId: String, sets: Int, reps: Int, weight: Double) =
        sets > 0 && reps > 0 && weight.isFinite() && weight >= 0 &&
            _data.value.exercises.any { it.id == exerciseId }

    private suspend fun replacePlan(updatedPlan: WorkoutPlan, imported: Exercise? = null) {
        val index = _data.value.plans.indexOfFirst { it.id == updatedPlan.id }
        storage?.savePlan(updatedPlan, imported)
        val current = _data.value
        val plans = current.plans.toMutableList()
        plans[index] = updatedPlan
        val exercises = if (imported == null) current.exercises else
            current.exercises.filterNot { it.id == imported.id } + imported
        _data.value = current.copy(plans = plans.toList(), exercises = exercises)
    }

    fun startWorkout(planId: String): WorkoutSession? {
        val current = _data.value
        if (changingPlan || current.isLoading || current.storageError) return null
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
        if (_data.value.isSavingWorkout) return false
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
        if (_data.value.isSavingWorkout) return false
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
        if (_data.value.isSavingWorkout) return false
        val current = _data.value
        if (current.activeSession == null) return false
        _data.value = current.copy(activeSession = null)
        return true
    }

    suspend fun finishWorkout(expectedSessionId: String? = null): WorkoutSession? = writes.withLock {
        val current = _data.value
        val session = current.activeSession ?: return@withLock null
        if (expectedSessionId != null && session.id != expectedSessionId) return@withLock null
        if (session.completedSets.isEmpty()) return@withLock null

        val finishedSession = session.copy(
            finishedAt = System.currentTimeMillis(),
            planName = current.plans.find { it.id == session.planId }?.name
        )
        _data.value = current.copy(isSavingWorkout = true)
        try {
            // Once saving starts, navigation/cancellation must not interrupt the commit-to-UI handoff.
            withContext(NonCancellable) {
                storage?.saveSession(finishedSession)
                _data.value = _data.value.copy(
                    sessions = _data.value.sessions + finishedSession,
                    activeSession = null
                )
            }
            finishedSession
        } finally {
            _data.value = _data.value.copy(isSavingWorkout = false)
        }
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
