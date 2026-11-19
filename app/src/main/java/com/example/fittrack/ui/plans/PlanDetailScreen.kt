package com.example.fittrack.ui.plans

import androidx.compose.foundation.layout.*
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

@Composable
fun PlanDetailScreen(viewModel: PlansViewModel, planId: String, onBack: () -> Unit) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val plan = plans.firstOrNull { it.id == planId }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (plan == null) {
            Text(stringResource(R.string.invalid_plan))
        } else {
            Text(plan.name, style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.training_day, dayName(plan.dayOfWeek)))
            if (plan.exercises.isEmpty()) Text(stringResource(R.string.no_plan_exercises))
            Button(onClick = { showAdd = true }, enabled = exercises.isNotEmpty()) {
                Text(stringResource(R.string.add_exercise))
            }
            if (exercises.isEmpty()) Text(stringResource(R.string.no_catalog_exercises))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(plan.exercises) { index, planned ->
                    val exercise = exercises.firstOrNull { it.id == planned.exerciseId }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(exercise?.name ?: stringResource(R.string.exercise_unavailable), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.exercise_target, planned.targetSets, planned.targetReps, planned.targetWeightKg))
                        }
                    }
                }
            }
        }
        if (showAdd && plan != null) AddExerciseDialog(
            exercises = exercises,
            onDismiss = { showAdd = false },
            onSave = { exerciseId, sets, reps, weight ->
                viewModel.addExercise(planId, exerciseId, sets, reps, weight).also { if (it) showAdd = false }
            }
        )
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}

@Composable
private fun AddExerciseDialog(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Double) -> Boolean
) {
    var selectedId by rememberSaveable { mutableStateOf(exercises.firstOrNull()?.id.orEmpty()) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var sets by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var saveError by rememberSaveable { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val effectiveId = selectedId.takeIf { id -> exercises.any { it.id == id } }
        ?: exercises.firstOrNull()?.id
    val parsedSets = sets.toIntOrNull()
    val parsedReps = reps.toIntOrNull()
    val parsedWeight = weight.trim().replace(',', '.').toDoubleOrNull()
    val setsInvalid = parsedSets == null || parsedSets <= 0
    val repsInvalid = parsedReps == null || parsedReps <= 0
    val weightInvalid = parsedWeight == null || !parsedWeight.isFinite() || parsedWeight < 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_exercise)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { menuOpen = true }) {
                        Text(exercises.firstOrNull { it.id == effectiveId }?.name ?: stringResource(R.string.select_exercise))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        exercises.forEach { exercise ->
                            DropdownMenuItem(text = { Text(exercise.name) }, onClick = {
                                selectedId = exercise.id
                                menuOpen = false
                            })
                        }
                    }
                }
                OutlinedTextField(sets, { sets = it }, label = { Text(stringResource(R.string.sets)) },
                    isError = attempted && setsInvalid,
                    supportingText = if (attempted && setsInvalid) {{ Text(stringResource(R.string.positive_sets_required)) }} else null)
                OutlinedTextField(reps, { reps = it }, label = { Text(stringResource(R.string.repetitions)) },
                    isError = attempted && repsInvalid,
                    supportingText = if (attempted && repsInvalid) {{ Text(stringResource(R.string.positive_reps_required)) }} else null)
                OutlinedTextField(weight, { weight = it }, label = { Text(stringResource(R.string.weight_kg)) },
                    isError = attempted && weightInvalid,
                    supportingText = if (attempted && weightInvalid) {{ Text(stringResource(R.string.valid_weight_required)) }} else null)
                if (exercises.isEmpty()) Text(stringResource(R.string.no_catalog_exercises))
                if (saveError) Text(stringResource(R.string.add_exercise_failed), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                attempted = true
                if (!setsInvalid && !repsInvalid && !weightInvalid && parsedSets != null && parsedReps != null && parsedWeight != null && effectiveId != null) {
                    saveError = !onSave(effectiveId, parsedSets, parsedReps, parsedWeight)
                }
            }, enabled = effectiveId != null) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
