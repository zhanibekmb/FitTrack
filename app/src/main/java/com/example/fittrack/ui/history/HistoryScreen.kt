package com.example.fittrack.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.data.model.*
import com.example.fittrack.ui.components.*

@Composable
fun HistoryScreen(vm: HistoryViewModel, openExercise: (String) -> Unit) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val exerciseIds = WorkoutStats.exerciseIds(sessions)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageHeader("История и прогресс", "Твои реальные результаты, а не только планы") }
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Завершено тренировок", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sessions.size.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Общий объём", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${weightText(WorkoutStats.volumeTotal(sessions))} кг", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { SectionTitle("Прогресс по упражнениям") }
        if (exerciseIds.isEmpty()) item {
            EmptyState("История пока пустая", "Выполните хотя бы один подход и завершите тренировку")
        }
        items(exerciseIds, key = { it }) { id ->
            Card(onClick = { openExercise(id) }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ExerciseLibrary.byId(id)?.name ?: id, fontWeight = FontWeight.SemiBold)
                        Text("Лучший вес: ${kg(WorkoutStats.bestWeight(sessions, id))}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("→")
                }
            }
        }
        item { SectionTitle("Последние тренировки") }
        items(sessions, key = { it.id }) { session ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(session.planName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(formatDate(session.completedAt ?: session.startedAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Объём: ${kg(WorkoutStats.volume(session))}")
                    session.exercises.forEach { ex ->
                        val completed = ex.sets.filter { it.completed }
                        if (completed.isNotEmpty()) {
                            Text("• ${ex.exerciseName}: ${completed.joinToString(" / ") { "${weightText(it.weightKg)}×${it.reps}" }}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
