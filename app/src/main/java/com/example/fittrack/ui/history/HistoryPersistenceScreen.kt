package com.example.fittrack.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fittrack.data.local.HistorySortOrder
import com.example.fittrack.data.local.SessionWithSets
import com.example.fittrack.data.remote.RemoteCatalogUiState
import java.util.Locale

@Composable
fun HistoryPersistenceScreen(vm: HistoryPersistenceViewModel) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val preferences by vm.preferences.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val remoteState by vm.remoteCatalogState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<SessionWithSets?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add workout")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Saved Workouts: ${stats.completedWorkoutsCount} · Volume: ${
                        String.format(Locale.ENGLISH, "%.1f", stats.totalVolumeKg)
                    } kg · Best: ${String.format(Locale.ENGLISH, "%.1f", stats.bestWeightKg)} kg",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = true,
                        onClick = { vm.toggleSortOrder() },
                        label = {
                            Text(
                                if (preferences.sortOrder == HistorySortOrder.NEWEST_FIRST) {
                                    "Sort: Newest"
                                } else {
                                    "Sort: Oldest"
                                }
                            )
                        }
                    )
                    FilterChip(
                        selected = preferences.showSetDetails,
                        onClick = { vm.toggleSetDetails() },
                        label = { Text("Sets: ${if (preferences.showSetDetails) "Shown" else "Hidden"}") }
                    )
                }
            }

            if (sessions.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("No saved workouts yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Tap + to add a workout record to Room and test persistence after restart.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(sessions, key = { it.session.id }) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.session.planName, style = MaterialTheme.typography.titleMedium)
                                Row {
                                    IconButton(onClick = { editingSession = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit workout")
                                    }
                                    IconButton(onClick = { vm.deleteWorkoutRecord(item.session.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete workout")
                                    }
                                }
                            }
                            if (item.session.notes.isNotBlank()) {
                                Text(item.session.notes, style = MaterialTheme.typography.bodySmall)
                            }
                            if (preferences.showSetDetails) {
                                item.sets.forEach { set ->
                                    Text(
                                        "${set.exerciseName}: Set ${set.setNumber} — ${set.weightKg} kg × ${set.reps}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("External Exercise Catalog (Retrofit)", style = MaterialTheme.typography.titleLarge)
            }

            when (val state = remoteState) {
                is RemoteCatalogUiState.Loading -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Loading exercises from REST API...")
                        }
                    }
                }

                is RemoteCatalogUiState.Error -> {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                                Button(onClick = { vm.loadRemoteExercises() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                is RemoteCatalogUiState.Empty -> {
                    item {
                        Text("No exercises available from API.")
                    }
                }

                is RemoteCatalogUiState.Success -> {
                    items(state.items, key = { it.id }) { exercise ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!exercise.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = exercise.imageUrl,
                                        contentDescription = exercise.name,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        exercise.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var planName by remember { mutableStateOf("Upper Body") }
        var exerciseName by remember { mutableStateOf("Bench Press") }
        var weightText by remember { mutableStateOf("60.0") }
        var repsText by remember { mutableStateOf("10") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Workout Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = planName, onValueChange = { planName = it }, label = { Text("Workout Title") })
                    OutlinedTextField(value = exerciseName, onValueChange = { exerciseName = it }, label = { Text("Exercise") })
                    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Weight (kg)") })
                    OutlinedTextField(value = repsText, onValueChange = { repsText = it }, label = { Text("Reps") })
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = weightText.toDoubleOrNull() ?: 0.0
                    val r = repsText.toIntOrNull() ?: 1
                    vm.addWorkoutRecord(planName, exerciseName, w, r, setsCount = 3, notes = notes)
                    showCreateDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    editingSession?.let { target ->
        var title by remember(target.session.id) { mutableStateOf(target.session.planName) }
        var notes by remember(target.session.id) { mutableStateOf(target.session.notes) }

        AlertDialog(
            onDismissRequest = { editingSession = null },
            title = { Text("Update Workout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Workout Title") })
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateWorkoutRecord(target.session, title, notes)
                    editingSession = null
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSession = null }) { Text("Cancel") }
            }
        )
    }
}