package com.example.fittrack.ui.workout

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.data.model.CompletedSet
import com.example.fittrack.data.model.PlannedExercise
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(
    planId: String,
    viewModel: WorkoutViewModel,
    onStart: () -> Unit,
    onFinished: () -> Unit
) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val plan = data.plans.find { it.id == planId }
    if (plan == null) {
        Text(stringResource(R.string.invalid_plan), modifier = Modifier.padding(20.dp))
        return
    }
    val active = data.activeSession
    val session = if (active?.planId == planId) active else null
    var finishError by rememberSaveable(session?.id) { mutableStateOf(false) }
    var restUntil by rememberSaveable(session?.id) { mutableLongStateOf(0L) }
    var remaining by remember { mutableIntStateOf(0) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(restUntil, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                val milliseconds = restUntil - SystemClock.elapsedRealtime()
                remaining = if (milliseconds <= 0) 0 else ((milliseconds + 999) / 1000).toInt()
                if (remaining == 0) break
                delay(250)
            }
        }
    }
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(plan.name, style = MaterialTheme.typography.headlineMedium) }
        if (session == null) {
            item {
                Text(stringResource(R.string.workout_recovery))
                Button(onClick = onStart) { Text(stringResource(R.string.start_workout)) }
            }
        } else {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.rest_timer), style = MaterialTheme.typography.titleMedium)
                        if (remaining > 0) {
                            Text(stringResource(R.string.timer_value, remaining / 60, remaining % 60))
                            TextButton(onClick = { restUntil = 0 }) { Text(stringResource(R.string.skip_rest)) }
                        } else {
                            OutlinedButton(onClick = { restUntil = SystemClock.elapsedRealtime() + 60_000 }) {
                                Text(stringResource(R.string.start_rest))
                            }
                        }
                    }
                }
            }
            items(plan.exercises.distinctBy { it.exerciseId }, key = { it.exerciseId }) { planned ->
                ExerciseRecorder(
                    sessionId = session.id,
                    planned = planned,
                    name = data.exercises.find { it.id == planned.exerciseId }?.name ?: stringResource(R.string.exercise_unavailable),
                    sets = session.completedSets.filter { it.exerciseId == planned.exerciseId },
                    onRecord = { number, weight, reps ->
                        val recorded = viewModel.recordSet(planId, planned.exerciseId, number, weight, reps)
                        if (recorded) {
                            restUntil = SystemClock.elapsedRealtime() + 60_000
                            finishError = false
                        }
                        recorded
                    }
                )
            }
            item {
                if (finishError) Text(stringResource(R.string.finish_failed), color = MaterialTheme.colorScheme.error)
                Button(onClick = {
                    if (viewModel.finishWorkout(planId)) onFinished() else finishError = true
                }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.finish_workout)) }
            }
        }
    }
}

@Composable
private fun ExerciseRecorder(
    sessionId: String, planned: PlannedExercise, name: String, sets: List<CompletedSet>,
    onRecord: (Int, Double, Int) -> Boolean
) {
    var weight by rememberSaveable(sessionId, planned.exerciseId) { mutableStateOf(planned.targetWeightKg.toString()) }
    var reps by rememberSaveable(sessionId, planned.exerciseId) { mutableStateOf(planned.targetReps.toString()) }
    var attempted by rememberSaveable(sessionId, planned.exerciseId) { mutableStateOf(false) }
    var failed by rememberSaveable(sessionId, planned.exerciseId) { mutableStateOf(false) }
    val parsedWeight = weight.trim().replace(',', '.').toDoubleOrNull()
    val parsedReps = reps.toIntOrNull()
    val invalidWeight = parsedWeight == null || !parsedWeight.isFinite() || parsedWeight < 0
    val invalidReps = parsedReps == null || parsedReps <= 0
    val number = (sets.maxOfOrNull { it.setNumber } ?: 0) + 1
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.exercise_target, planned.targetSets, planned.targetReps, planned.targetWeightKg),
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            for (set in sets) {
                Text(stringResource(R.string.completed_set, set.setNumber, set.weightKg, set.reps))
            }
            Text(stringResource(R.string.set_number, number), style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(weight, {
                weight = it
                failed = false
            }, modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.weight_kg)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = attempted && invalidWeight,
                supportingText = if (attempted && invalidWeight) {{ Text(stringResource(R.string.valid_weight_required)) }} else null)
            OutlinedTextField(reps, {
                reps = it
                failed = false
            }, modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.repetitions)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = attempted && invalidReps,
                supportingText = if (attempted && invalidReps) {{ Text(stringResource(R.string.positive_reps_required)) }} else null)
            if (failed) Text(stringResource(R.string.set_record_failed), color = MaterialTheme.colorScheme.error)
            FilledTonalButton(onClick = {
                attempted = true
                if (!invalidWeight && !invalidReps) {
                    failed = !onRecord(number, parsedWeight, parsedReps)
                    if (!failed) attempted = false
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.record_set)) }
        }
    }
}

