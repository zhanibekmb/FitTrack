package com.example.fittrack.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.activeSession, repository.plans, repository.exercises, repository.sessions) { session, plans, library, _ ->
                Triple(session, plans, library)
            }.collect { (session, plans, library) ->
                val plan = plans.find { it.id == session?.planId }
                if (session == null || plan == null) {
                    _uiState.value = WorkoutUiState()
                } else {
                    val old = _uiState.value.takeIf { it.sessionId == session.id }
                    val exercises = workoutExercises(plan, library, session.completedSets).map { exercise ->
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
        val state = _uiState.value
        if (state.sessionId == null || repository.activeSession.value?.id != state.sessionId) return
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
        } else {
            _uiState.value = _uiState.value.copy(message = WorkoutMessage.SAVE_FAILED)
        }
    }

    fun addSet(exerciseId: String) {
        val state = _uiState.value
        if (state.sessionId == null || repository.activeSession.value?.id != state.sessionId) return
        _uiState.value = state.copy(message = null, exercises = state.exercises.map { exercise ->
            if (exercise.id != exerciseId) exercise else {
                val last = exercise.sets.last()
                val next = last.copy(number = exercise.sets.maxOf { it.number } + 1,
                    completed = false, showErrors = false, previous = null)
                exercise.copy(sets = exercise.sets + next)
            }
        })
    }

    private fun changeSet(exerciseId: String, number: Int, change: (WorkoutSetUiState) -> WorkoutSetUiState) {
        val state = _uiState.value
        if (state.sessionId == null || repository.activeSession.value?.id != state.sessionId) return
        _uiState.value = state.copy(message = null, exercises = state.exercises.map { exercise ->
            if (exercise.id != exerciseId) exercise else exercise.copy(sets = exercise.sets.map { row ->
                if (row.number == number) change(row) else row
            })
        })
    }
}
