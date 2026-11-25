package com.example.fittrack.ui.workout

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class WorkoutViewModel(
    private val repository: WorkoutRepository,
    elapsedRealtime: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModel() {
    private val timer = RestTimer(viewModelScope, elapsedRealtime)
    val restTimer = timer.state
    fun setRestDuration(seconds: Int): Boolean = timer.setDuration(seconds)
    fun startRest() = timer.start()
    fun pauseRest() = timer.pause()
    fun resetRest() = timer.reset()
    fun skipRest() = timer.skip()

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()

    init {
        viewModelScope.launch {
            repository.data.collect { data ->
                val session = data.activeSession
                val plan = data.plans.find { it.id == session?.planId }
                if (session == null || plan == null) {
                    timer.reset()
                    _uiState.value = WorkoutUiState()
                } else {
                    val old = _uiState.value.takeIf { it.sessionId == session.id }
                    if (old == null) timer.reset()
                    val exercises = workoutExercises(plan, data.exercises, session.completedSets).map { exercise ->
                        val drafts = old?.exercises?.find { it.id == exercise.id }?.sets.orEmpty()
                        val rows = exercise.sets.map { row ->
                            if (row.completed) row else drafts.find { it.number == row.number }?.copy(completed = false) ?: row
                        }
                        val previous = repository.getPreviousSets(exercise.id).associateBy { it.setNumber }
                        val allRows = rows + drafts.filter { draft -> rows.none { it.number == draft.number } }
                        exercise.copy(sets = allRows.map { it.copy(previous = previous[it.number]) })
                    }
                    _uiState.value = WorkoutUiState(session.id, plan.id, plan.name, exercises, old?.message)
                }
            }
        }
    }

    fun updateWeight(exerciseId: String, number: Int, value: String) {
        changeSet(exerciseId, number) { row ->
            if (row.completed) row else row.copy(weight = value, showErrors = false)
        }
    }

    fun updateReps(exerciseId: String, number: Int, value: String) {
        changeSet(exerciseId, number) { row ->
            if (row.completed) row else row.copy(reps = value, showErrors = false)
        }
    }

    fun setCompleted(exerciseId: String, number: Int, completed: Boolean) {
        if (_saving.value) return
        val state = _uiState.value
        if (state.sessionId == null || repository.data.value.activeSession?.id != state.sessionId) return
        val row = state.exercises.find { it.id == exerciseId }?.sets?.find { it.number == number } ?: return
        if (row.completed == completed) return
        val weight = parsedWeight(row.weight)
        val reps = parsedReps(row.reps)
        if (completed && (weight == null || reps == null)) {
            changeSet(exerciseId, number) { it.copy(showErrors = true) }
            _uiState.value = _uiState.value.copy(message = WorkoutMessage.INVALID_INPUT)
            return
        }
        val saved = if (completed) {
            repository.completeSet(exerciseId, number, weight ?: return, reps ?: return)
        } else {
            repository.removeCompletedSet(exerciseId, number)
        }
        if (saved) {
            changeSet(exerciseId, number) { it.copy(completed = completed, showErrors = false) }
            if (completed) timer.restart()
        } else {
            _uiState.value = _uiState.value.copy(message = WorkoutMessage.SAVE_FAILED)
        }
    }

    fun addSet(exerciseId: String) {
        if (_saving.value) return
        val state = _uiState.value
        if (state.sessionId == null || repository.data.value.activeSession?.id != state.sessionId) return
        _uiState.value = state.copy(message = null, exercises = state.exercises.map { exercise ->
            if (exercise.id != exerciseId) exercise else {
                val last = exercise.sets.last()
                val nextNumber = exercise.sets.maxOf { it.number } + 1
                val previous = repository.getPreviousSets(exerciseId).find { it.setNumber == nextNumber }
                val next = last.copy(number = nextNumber,
                    completed = false, showErrors = false, previous = previous)
                exercise.copy(sets = exercise.sets + next)
            }
        })
    }

    fun startWorkout(planId: String): Boolean {
        val plan = repository.data.value.plans.find { it.id == planId }
        if (plan == null || plan.exercises.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = if (plan == null)
                WorkoutMessage.SESSION_UNAVAILABLE else WorkoutMessage.EMPTY_PLAN)
            return false
        }
        val active = repository.data.value.activeSession
        val success = if (active == null) repository.startWorkout(planId) != null else active.planId == planId
        if (!success) _uiState.value = _uiState.value.copy(message = WorkoutMessage.SESSION_UNAVAILABLE)
        return success
    }

    suspend fun finishWorkout(planId: String): Boolean {
        if (_saving.value) return false
        val active = repository.data.value.activeSession
        if (active == null || active.planId != planId || active.id != _uiState.value.sessionId) {
            _uiState.value = _uiState.value.copy(message = WorkoutMessage.SESSION_UNAVAILABLE)
            return false
        }
        if (active.completedSets.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = WorkoutMessage.EMPTY_WORKOUT)
            return false
        }
        _saving.value = true
        try {
            if (repository.finishWorkout(active.id) == null) {
                _uiState.value = _uiState.value.copy(message = WorkoutMessage.SESSION_UNAVAILABLE)
                return false
            }
            timer.reset()
            _uiState.value = WorkoutUiState()
            return true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(message = WorkoutMessage.SAVE_FAILED)
            return false
        } finally {
            _saving.value = false
        }
    }

    fun finishWorkout(planId: String, onFinished: () -> Unit) {
        viewModelScope.launch { if (finishWorkout(planId)) onFinished() }
    }

    fun discardWorkout(planId: String): Boolean {
        if (_saving.value) return false
        val active = repository.data.value.activeSession
        if (active == null || active.planId != planId || active.id != _uiState.value.sessionId) return false
        if (!repository.discardWorkout()) return false
        timer.reset()
        _uiState.value = WorkoutUiState()
        return true
    }

    private fun changeSet(exerciseId: String, number: Int, change: (WorkoutSetUiState) -> WorkoutSetUiState) {
        if (_saving.value) return
        val state = _uiState.value
        if (state.sessionId == null || repository.data.value.activeSession?.id != state.sessionId) return
        _uiState.value = state.copy(message = null, exercises = state.exercises.map { exercise ->
            if (exercise.id != exerciseId) exercise else exercise.copy(sets = exercise.sets.map { row ->
                if (row.number == number) change(row) else row
            })
        })
    }
}
