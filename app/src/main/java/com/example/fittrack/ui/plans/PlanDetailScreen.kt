package com.example.fittrack.ui.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R

@Composable
fun PlanDetailScreen(viewModel: PlansViewModel, planId: String, onBack: () -> Unit) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val plan = plans.firstOrNull { it.id == planId }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (plan == null) {
            Text(stringResource(R.string.invalid_plan))
        } else {
            Text(plan.name, style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.training_day, dayName(plan.dayOfWeek)))
            if (plan.exercises.isEmpty()) Text(stringResource(R.string.no_plan_exercises))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(plan.exercises) { index, planned ->
                    val exercise = exercises.firstOrNull { it.id == planned.exerciseId }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(exercise?.name ?: stringResource(R.string.exercise_unavailable), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.exercise_target, planned.targetSets, planned.targetReps, planned.targetWeightKg))
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}
