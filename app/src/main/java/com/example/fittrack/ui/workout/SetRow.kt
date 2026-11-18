package com.example.fittrack.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fittrack.R

@Composable
fun SetRow(
    set: WorkoutSetUiState,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    canEditWeight: Boolean = false,
    canEditReps: Boolean = false
) {
    val description = stringResource(R.string.set_completed_description, set.number)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.set_number, set.number), modifier = Modifier.weight(1f))
            Checkbox(
                checked = set.completed,
                onCheckedChange = onCompletedChange,
                modifier = Modifier.semantics { contentDescription = description }
            )
        }
        Text(
            text = set.previous?.let {
                stringResource(R.string.previous_set, weightInput(it.weightKg), it.reps)
            } ?: stringResource(R.string.previous_set_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = set.weight, onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.weight_kg)) },
                enabled = canEditWeight && !set.completed,
                isError = set.showErrors && parsedWeight(set.weight) == null,
                supportingText = if (set.showErrors && parsedWeight(set.weight) == null) {
                    { Text(stringResource(R.string.valid_weight_required)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = set.reps, onValueChange = onRepsChange,
                label = { Text(stringResource(R.string.repetitions)) },
                enabled = canEditReps && !set.completed,
                isError = set.showErrors && parsedReps(set.reps) == null,
                supportingText = if (set.showErrors && parsedReps(set.reps) == null) {
                    { Text(stringResource(R.string.positive_reps_required)) }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
