package com.example.fittrack.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressScreen(
    exerciseId: String,
    vm: HistoryViewModel,
    onBack: () -> Unit
) {
    val sessions by vm.sessions.collectAsState()

    val exerciseSets = sessions.map { session ->
        session to session.exercises.filter { it.exerciseId == exerciseId && it.completed }
    }.filter { it.second.isNotEmpty() }

    val weights = exerciseSets.map { (_, sets) -> sets.maxOf { it.weightKg } }
    val maxWeight = weights.maxOrNull() ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Прогресс упражнения") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Максимальный вес: ${maxWeight.toInt()} кг",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressChart(values = weights)
                    }
                }
            }

            item {
                Text(
                    text = "История результатов",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (exerciseSets.isEmpty()) {
                item {
                    Text("Пока нет выполненных подходов для этого упражнения.")
                }
            } else {
                items(exerciseSets) { (session, sets) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = session.date, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            sets.forEach { set ->
                                Text(text = "• Вес: ${set.weightKg} кг | Повторений: ${set.reps}")
                            }
                        }
                    }
                }
            }
        }
    }
}

