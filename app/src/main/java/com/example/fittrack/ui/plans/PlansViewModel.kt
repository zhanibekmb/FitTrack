package com.example.fittrack.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlanActionState(val saving: Boolean = false, val failed: Boolean = false)

class PlansViewModel(private val repository: WorkoutRepository = FitTrackContainer.repository) : ViewModel() {
    val data = repository.data
    private val _action = MutableStateFlow(PlanActionState())
    val action = _action.asStateFlow()

    fun createPlan(name: String, dayOfWeek: Int, onSaved: (String) -> Unit) = save {
        onSaved(repository.createPlan(name, dayOfWeek).id)
        true
    }

    fun editPlan(planId: String, name: String, dayOfWeek: Int, onSaved: () -> Unit) = save {
        repository.editPlan(planId, name, dayOfWeek).also { if (it) onSaved() }
    }

    fun deletePlan(planId: String, onDeleted: () -> Unit) = save {
        repository.deletePlan(planId).also { if (it) onDeleted() }
    }

    fun addExercise(planId: String, exerciseId: String, sets: Int, reps: Int, weightKg: Double,
                    onSaved: () -> Unit) = save {
        repository.addExerciseToPlan(planId, exerciseId, sets, reps, weightKg).also { if (it) onSaved() }
    }

    fun editExercise(planId: String, index: Int, exercise: PlannedExercise, onSaved: () -> Unit) = save {
        repository.editPlannedExercise(planId, index, exercise).also { if (it) onSaved() }
    }

    fun addCatalogExercise(planId: String, exercise: CatalogExercise, sets: Int, reps: Int,
                           weightKg: Double, onSaved: () -> Unit) = save {
        repository.addCatalogExerciseToPlan(planId, exercise, sets, reps, weightKg).also { if (it) onSaved() }
    }

    fun removeExercise(planId: String, index: Int) = save { repository.removePlannedExercise(planId, index) }

    private fun save(action: suspend () -> Boolean) {
        if (_action.value.saving) return
        _action.value = PlanActionState(saving = true)
        viewModelScope.launch {
            try {
                _action.value = PlanActionState(failed = !action())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _action.value = PlanActionState(failed = true)
            } finally {
                _action.value = _action.value.copy(saving = false)
            }
        }
    }
}
