package com.example.fittrack.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R

@Composable
fun WorkoutScreen(planId: String, viewModel: WorkoutViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text(stringResource(R.string.active_workout), style = MaterialTheme.typography.headlineMedium) }
        if (uiState.planId == planId) {
            item {
                Text(uiState.planName, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.workout_set_count, uiState.completedSets, uiState.totalSets))
            }
            items(uiState.exercises, key = { it.id }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onWeightChange = { number, value -> viewModel.updateWeight(exercise.id, number, value) },
                    canEditWeight = true,
                    onRepsChange = { number, value -> viewModel.updateReps(exercise.id, number, value) },
                    canEditReps = true,
                    onCompletedChange = { _, _ -> }
                )
            }
        } else {
            item { Text(stringResource(R.string.workout_recovery)) }
        }
        item { OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
    }
}
