package com.example.fittrack.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExerciseCard(
    exercise: WorkoutExerciseUiState,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onCompletedChange: (Int, Boolean) -> Unit,
    canEditWeight: Boolean = false,
    canEditReps: Boolean = false
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleLarge)
            Text(exercise.muscleGroup, color = MaterialTheme.colorScheme.onSurfaceVariant)
            exercise.sets.forEach { set ->
                key(set.number) {
                    SetRow(set, { onWeightChange(set.number, it) }, { onRepsChange(set.number, it) },
                        { onCompletedChange(set.number, it) }, canEditWeight, canEditReps)
                }
            }
        }
    }
}
