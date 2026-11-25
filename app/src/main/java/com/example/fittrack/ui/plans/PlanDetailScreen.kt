package com.example.fittrack.ui.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.model.PlannedExercise

@Composable
fun PlanDetailScreen(
    viewModel: PlansViewModel,
    planId: String,
    onStartWorkout: () -> Unit,
    onEditPlan: () -> Unit,
    onDeleted: () -> Unit
) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val action by viewModel.action.collectAsStateWithLifecycle()
    val exercises = data.exercises
    val plan = data.plans.find { it.id == planId }
    val hasActiveWorkout = data.activeSession?.planId == planId
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var editingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (action.failed) item { Text(stringResource(R.string.storage_save_failed), color = MaterialTheme.colorScheme.error) }
        if (plan == null) item { Text(stringResource(R.string.invalid_plan)) }
        else {
            item { Text(plan.name, style = MaterialTheme.typography.headlineMedium) }
            item { Text(stringResource(R.string.training_day, dayName(plan.dayOfWeek)), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (plan.exercises.isEmpty()) item { Text(stringResource(R.string.no_plan_exercises)) }
            item {
                Button(onClick = onStartWorkout, enabled = plan.exercises.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.start_workout))
                }
                if (plan.exercises.isEmpty()) Text(stringResource(R.string.empty_plan_hint), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                FilledTonalButton(onClick = { editingIndex = null; showAdd = true }, enabled = exercises.isNotEmpty() && !hasActiveWorkout && !action.saving, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.add_exercise))
                }
                if (exercises.isEmpty()) Text(stringResource(R.string.no_catalog_exercises))
            }
            itemsIndexed(plan.exercises) { index, planned ->
                val exercise = exercises.firstOrNull { it.id == planned.exerciseId }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(exercise?.name ?: stringResource(R.string.exercise_unavailable), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.exercise_target, planned.targetSets, planned.targetReps, planned.targetWeightKg),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row {
                            TextButton(onClick = { editingIndex = index; showAdd = true }, enabled = !hasActiveWorkout && !action.saving) {
                                Text(stringResource(R.string.edit_exercise))
                            }
                            TextButton(onClick = { viewModel.removeExercise(planId, index) }, enabled = !hasActiveWorkout && !action.saving) {
                                Text(stringResource(R.string.remove_exercise))
                            }
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onEditPlan, enabled = !hasActiveWorkout && !action.saving, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.edit_plan))
                }
                OutlinedButton(
                    onClick = { showDelete = true },
                    enabled = !hasActiveWorkout && !action.saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_plan))
                }
                if (hasActiveWorkout) Text(stringResource(R.string.delete_active_plan_hint))
            }
        }
    }
    if (showDelete && plan != null && !hasActiveWorkout) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.delete_plan)) },
            text = { Text(stringResource(R.string.delete_plan_confirmation, plan.name)) },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    viewModel.deletePlan(planId, onDeleted)
                }) {
                    Text(stringResource(R.string.delete_plan))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    if (showAdd && plan != null) AddExerciseDialog(
        exercises = exercises,
        initial = editingIndex?.let { plan.exercises.getOrNull(it) },
        action = action,
        onDismiss = { showAdd = false },
        onSave = { exerciseId, sets, reps, weight ->
            val index = editingIndex
            if (index == null) viewModel.addExercise(planId, exerciseId, sets, reps, weight) { showAdd = false }
            else viewModel.editExercise(planId, index, PlannedExercise(exerciseId, sets, reps, weight)) { showAdd = false }
        }
    )
}
@Composable
private fun AddExerciseDialog(
    exercises: List<Exercise>,
    initial: PlannedExercise?,
    action: PlanActionState,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Double) -> Unit
) {
    var selectedId by rememberSaveable { mutableStateOf(initial?.exerciseId ?: exercises.firstOrNull()?.id.orEmpty()) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var sets by rememberSaveable { mutableStateOf(initial?.targetSets?.toString().orEmpty()) }
    var reps by rememberSaveable { mutableStateOf(initial?.targetReps?.toString().orEmpty()) }
    var weight by rememberSaveable { mutableStateOf(initial?.targetWeightKg?.toString().orEmpty()) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val selectedExercise = exercises.find { it.id == selectedId } ?: exercises.firstOrNull()
    val effectiveId = selectedExercise?.id
    val parsedSets = sets.toIntOrNull()
    val parsedReps = reps.toIntOrNull()
    val parsedWeight = weight.trim().replace(',', '.').toDoubleOrNull()
    val setsInvalid = parsedSets == null || parsedSets <= 0
    val repsInvalid = parsedReps == null || parsedReps <= 0
    val weightInvalid = parsedWeight == null || !parsedWeight.isFinite() || parsedWeight < 0
    AlertDialog(
        onDismissRequest = { if (!action.saving) onDismiss() },
        title = { Text(stringResource(if (initial == null) R.string.add_exercise else R.string.edit_exercise)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedButton(onClick = { menuOpen = true }) {
                        Text(selectedExercise?.name ?: stringResource(R.string.select_exercise))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        for (exercise in exercises) {
                            DropdownMenuItem(text = { Text(exercise.name) }, onClick = {
                                selectedId = exercise.id
                                menuOpen = false
                            })
                        }
                    }
                }
                OutlinedTextField(sets, { sets = it }, label = { Text(stringResource(R.string.sets)) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), isError = attempted && setsInvalid,
                    supportingText = if (attempted && setsInvalid) {{ Text(stringResource(R.string.positive_sets_required)) }} else null)
                OutlinedTextField(reps, { reps = it }, label = { Text(stringResource(R.string.repetitions)) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), isError = attempted && repsInvalid,
                    supportingText = if (attempted && repsInvalid) {{ Text(stringResource(R.string.positive_reps_required)) }} else null)
                OutlinedTextField(weight, { weight = it }, label = { Text(stringResource(R.string.weight_kg)) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), isError = attempted && weightInvalid,
                    supportingText = if (attempted && weightInvalid) {{ Text(stringResource(R.string.valid_weight_required)) }} else null)
                if (exercises.isEmpty()) Text(stringResource(R.string.no_catalog_exercises))
                if (action.failed) Text(stringResource(R.string.storage_save_failed), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (!setsInvalid && !repsInvalid && !weightInvalid && effectiveId != null) {
                    onSave(effectiveId, parsedSets, parsedReps, parsedWeight)
                }
            }, enabled = effectiveId != null && !action.saving) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !action.saving) { Text(stringResource(R.string.cancel)) } }
    )
}
