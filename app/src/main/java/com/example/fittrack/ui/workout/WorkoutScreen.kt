package com.example.fittrack.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fittrack.R

@Composable
fun WorkoutScreen(uiState: WorkoutUiState, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.active_workout), style = MaterialTheme.typography.headlineMedium)
        Text(uiState.planName, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.workout_set_count, uiState.completedSets, uiState.totalSets))
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}
