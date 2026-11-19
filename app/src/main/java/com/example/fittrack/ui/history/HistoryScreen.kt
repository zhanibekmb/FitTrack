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
import com.example.fittrack.ui.components.StatCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatWorkoutDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.ENGLISH).format(Date(timestamp))

@Composable
fun HistoryScreen(vm: HistoryViewModel, openExercise: (String) -> Unit) {
    val allSessions by vm.sessions.collectAsStateWithLifecycle()
    val plans by vm.plans.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val sessions = allSessions.filter { it.isFinished }.sortedByDescending { it.finishedAt }
    val exerciseIds = sessions.flatMap { it.completedSets }.map { it.exerciseId }.distinct()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { StatCard(stringResource(R.string.workouts_count), sessions.size.toString(), Modifier.fillMaxWidth()) }
        item {
            StatCard(stringResource(R.string.total_volume),
                stringResource(R.string.weight_value, sessions.sumOf { s -> s.completedSets.sumOf { it.volumeKg } }),
                Modifier.fillMaxWidth())
        }
        if (sessions.isEmpty()) item {
            EmptyState(stringResource(R.string.no_history), stringResource(R.string.no_history_hint))
        }
        if (exerciseIds.isNotEmpty()) item {
            Text(stringResource(R.string.exercise_progress), style = MaterialTheme.typography.titleLarge)
        }
        items(exerciseIds, key = { it }) { id ->
            Card(onClick = { openExercise(id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(exercises.find { it.id == id }?.name ?: stringResource(R.string.exercise_unavailable),
                        style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.best_weight, sessions.flatMap { it.completedSets }
                        .filter { it.exerciseId == id }.maxOf { it.weightKg }),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.view_progress), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (sessions.isNotEmpty()) item {
            Text(stringResource(R.string.recent_workouts), style = MaterialTheme.typography.titleLarge)
        }
        items(sessions, key = { it.id }) { session ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plans.find { it.id == session.planId }?.name ?: stringResource(R.string.workout),
                        style = MaterialTheme.typography.titleMedium)
                    Text(formatWorkoutDate(session.finishedAt ?: session.startedAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.session_volume, session.completedSets.sumOf { it.volumeKg }))
                    session.completedSets.groupBy { it.exerciseId }.forEach { (id, sets) ->
                        Text(exercises.find { it.id == id }?.name ?: stringResource(R.string.exercise_unavailable),
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
