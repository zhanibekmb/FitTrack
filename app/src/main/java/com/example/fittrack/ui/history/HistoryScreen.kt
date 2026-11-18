package com.example.fittrack.ui.history

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
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onOpenExercise: (String) -> Unit, onBack: () -> Unit) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val finished = sessions.filter { it.isFinished }.sortedByDescending { it.finishedAt }
    val exerciseIds = finished.flatMap { it.completedSets }.map { it.exerciseId }.distinct()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(stringResource(R.string.workout_history), style = MaterialTheme.typography.headlineMedium) }
        item { Text(stringResource(R.string.history_summary, finished.size, finished.sumOf { session -> session.completedSets.sumOf { it.volumeKg } })) }
        if (finished.isEmpty()) item { Text(stringResource(R.string.history_empty)) }
        items(exerciseIds, key = { it }) { id ->
            OutlinedButton(onClick = { onOpenExercise(id) }, modifier = Modifier.fillMaxWidth()) {
                Text(exercises.find { it.id == id }?.name ?: id)
            }
        }
        items(finished, key = { it.id }) { session ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plans.find { it.id == session.planId }?.name ?: session.planId, style = MaterialTheme.typography.titleMedium)
                    Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.finishedAt ?: session.startedAt)))
                    session.completedSets.groupBy { it.exerciseId }.forEach { (id, sets) ->
                        Text(exercises.find { it.id == id }?.name ?: id)
                        sets.sortedBy { it.setNumber }.forEach { set ->
                            Text(stringResource(R.string.history_set, set.setNumber, set.weightKg, set.reps))
                        }
                    }
                }
            }
        }
        item { OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
    }
}
