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
fun ExerciseProgressScreen(exerciseId: String, vm: HistoryViewModel, onBack: () -> Unit) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val exercises by vm.exercises.collectAsStateWithLifecycle()
    val results = sessions.filter { it.isFinished }.sortedBy { it.finishedAt }.mapNotNull { session ->
        val sets = session.completedSets.filter { it.exerciseId == exerciseId }
        if (sets.isEmpty()) null else session to sets
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(exercises.find { it.id == exerciseId }?.name ?: exerciseId, style = MaterialTheme.typography.headlineMedium) }
        item { Text(stringResource(R.string.exercise_progress)) }
        item { ProgressChart(results.map { (_, sets) -> sets.maxOf { it.weightKg } }) }
        items(results, key = { it.first.id }) { (session, sets) ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(DateFormat.getDateInstance().format(Date(session.finishedAt ?: session.startedAt)))
                    sets.sortedBy { it.setNumber }.forEach { set ->
                        Text(stringResource(R.string.history_set, set.setNumber, set.weightKg, set.reps))
                    }
                }
            }
        }
        item { OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
    }
}
