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
import com.example.fittrack.ui.components.EmptyState

@Composable
fun ExerciseProgressScreen(exerciseId: String, vm: HistoryViewModel) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val results = sessions.filter { it.isFinished }.sortedBy { it.finishedAt }.map { session ->
        session to session.completedSets.filter { it.exerciseId == exerciseId }
    }.filter { it.second.isNotEmpty() }
    val weights = results.map { (_, sets) -> sets.maxOf { it.weightKg } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(exercises.find { it.id == exerciseId }?.name ?: stringResource(R.string.exercise_unavailable),
                style = MaterialTheme.typography.titleLarge)
        }
        if (results.isEmpty()) item {
            EmptyState(stringResource(R.string.no_exercise_results), stringResource(R.string.no_history_hint))
        } else {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.best_weight, weights.maxOrNull() ?: 0.0),
                            style = MaterialTheme.typography.titleMedium)
                        ProgressChart(weights)
                    }
                }
            }
            item { Text(stringResource(R.string.result_history), style = MaterialTheme.typography.titleLarge) }
            items(results.asReversed(), key = { it.first.id }) { (session, sets) ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(formatWorkoutDate(session.finishedAt ?: session.startedAt),
                            style = MaterialTheme.typography.labelLarge)
                        sets.forEach { set ->
                            Text(stringResource(R.string.completed_set, set.setNumber, set.weightKg, set.reps))
                        }
                    }
                }
            }
        }
    }
}
