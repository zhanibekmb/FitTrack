package com.example.fittrack.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R

@Composable
fun WorkoutScreen(planId: String, viewModel: WorkoutViewModel, onBack: () -> Unit, onFinished: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState by viewModel.restTimer.collectAsStateWithLifecycle()
    var confirmFinish by rememberSaveable(uiState.sessionId) { mutableStateOf(false) }
    var confirmDiscard by rememberSaveable(uiState.sessionId) { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text(stringResource(R.string.active_workout), style = MaterialTheme.typography.headlineMedium) }
        uiState.message?.let { message ->
            item {
                Text(stringResource(when (message) {
                    WorkoutMessage.INVALID_INPUT -> R.string.workout_invalid_input
                    WorkoutMessage.SAVE_FAILED -> R.string.workout_save_failed
                    WorkoutMessage.EMPTY_WORKOUT -> R.string.workout_empty_finish
                    WorkoutMessage.EMPTY_PLAN -> R.string.workout_empty_plan
                    WorkoutMessage.SESSION_UNAVAILABLE -> R.string.workout_recovery
                }), color = MaterialTheme.colorScheme.error)
            }
        }
        if (uiState.planId == planId) {
            item {
                Text(uiState.planName, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.workout_set_count, uiState.completedSets, uiState.totalSets))
                LinearProgressIndicator(
                    progress = { if (uiState.totalSets == 0) 0f else uiState.completedSets.toFloat() / uiState.totalSets },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            item {
                RestTimerCard(timerState, viewModel::setRestDuration, viewModel::startRest,
                    viewModel::pauseRest, viewModel::resetRest, viewModel::skipRest)
            }
            items(uiState.exercises, key = { it.id }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onWeightChange = { number, value -> viewModel.updateWeight(exercise.id, number, value) },
                    onRepsChange = { number, value -> viewModel.updateReps(exercise.id, number, value) },
                    onCompletedChange = { number, completed -> viewModel.setCompleted(exercise.id, number, completed) },
                    onAddSet = { viewModel.addSet(exercise.id) }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.message == WorkoutMessage.EMPTY_WORKOUT) {
                        Text(stringResource(R.string.workout_empty_finish), color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = {
                        if (uiState.completedSets == 0) viewModel.finishWorkout(planId) else confirmFinish = true
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.finish_workout)) }
                    TextButton(onClick = { confirmDiscard = true }) { Text(stringResource(R.string.discard_workout)) }
                }
            }
        } else if (uiState.sessionId == null) {
            item {
                Text(stringResource(R.string.workout_recovery))
                Button(onClick = { viewModel.startWorkout(planId) }) { Text(stringResource(R.string.start_workout)) }
            }
        } else {
            item { Text(stringResource(R.string.workout_conflict)) }
        }
        item { OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
    }
    if (confirmFinish && uiState.planId == planId) AlertDialog(
        onDismissRequest = { confirmFinish = false },
        title = { Text(stringResource(R.string.finish_workout)) },
        text = { Text(stringResource(R.string.finish_workout_question, uiState.completedSets, uiState.totalSets)) },
        confirmButton = {
            TextButton(onClick = {
                confirmFinish = false
                if (viewModel.finishWorkout(planId)) onFinished()
            }) { Text(stringResource(R.string.finish_workout)) }
        },
        dismissButton = { TextButton(onClick = { confirmFinish = false }) { Text(stringResource(R.string.cancel)) } }
    )
    if (confirmDiscard && uiState.planId == planId) AlertDialog(
        onDismissRequest = { confirmDiscard = false },
        title = { Text(stringResource(R.string.discard_workout)) },
        text = { Text(stringResource(R.string.discard_workout_question)) },
        confirmButton = {
            TextButton(onClick = {
                confirmDiscard = false
                if (viewModel.discardWorkout(planId)) onFinished()
            }) { Text(stringResource(R.string.discard_workout)) }
        },
        dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.cancel)) } }
    )
}
