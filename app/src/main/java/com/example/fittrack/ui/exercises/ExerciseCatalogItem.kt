package com.example.fittrack.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fittrack.R
import com.example.fittrack.data.model.CatalogCredit
import com.example.fittrack.data.model.CatalogExercise

@Composable
internal fun ExerciseCatalogItem(exercise: CatalogExercise, onSelect: () -> Unit) {
    var showCredits by rememberSaveable { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            val muscleGroup = exercise.muscleGroup
                ?: stringResource(R.string.exercise_catalog_unknown_muscles)
            Text(muscleGroup, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (exercise.equipment.isNotEmpty()) {
                Text(
                    stringResource(R.string.exercise_catalog_equipment, exercise.equipment.joinToString()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { showCredits = true }) {
                    Text(stringResource(R.string.exercise_catalog_credits))
                }
                Button(onClick = onSelect) {
                    Text(stringResource(R.string.exercise_catalog_select))
                }
            }
        }
    }
    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            title = { Text(stringResource(R.string.exercise_catalog_credits)) },
            text = { CreditDetails(exercise.credit, stringResource(R.string.exercise_catalog_wger_source)) },
            confirmButton = {
                TextButton(onClick = { showCredits = false }) {
                    Text(stringResource(R.string.exercise_catalog_close))
                }
            }
        )
    }
}

@Composable
private fun CreditDetails(credit: CatalogCredit, sourceLabel: String) {
    val uriHandler = LocalUriHandler.current
    Column {
        Text(credit.title, style = MaterialTheme.typography.titleSmall)
        if (credit.authors.isNotEmpty()) {
            Text(stringResource(R.string.exercise_catalog_credit_by, credit.authors.joinToString()))
        }
        TextButton(onClick = { uriHandler.openUri(credit.sourceUrl) }) { Text(sourceLabel) }
        credit.license?.let { license ->
            TextButton(onClick = { uriHandler.openUri(license.url) }) { Text(license.name) }
        }
    }
}
