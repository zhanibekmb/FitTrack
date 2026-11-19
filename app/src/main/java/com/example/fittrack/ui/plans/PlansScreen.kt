package com.example.fittrack.ui.plans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R

@Composable
fun PlansScreen(viewModel: PlansViewModel, onOpenPlan: (String) -> Unit, onCreatePlan: () -> Unit, onBack: () -> Unit) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.workout_plans), style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onCreatePlan) { Text(stringResource(R.string.create_plan)) }
        if (plans.isEmpty()) Text(stringResource(R.string.no_plans))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(plans, key = { it.id }) { plan ->
                Card(Modifier.fillMaxWidth().clickable { onOpenPlan(plan.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(plan.name, style = MaterialTheme.typography.titleLarge)
                        Text(dayName(plan.dayOfWeek))
                        Text(pluralStringResource(R.plurals.plans_exercise_count, plan.exercises.size, plan.exercises.size))
                    }
                }
            }
        }
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}

@Composable
fun dayName(dayOfWeek: Int): String = when (dayOfWeek) {
    1 -> stringResource(R.string.monday)
    2 -> stringResource(R.string.tuesday)
    3 -> stringResource(R.string.wednesday)
    4 -> stringResource(R.string.thursday)
    5 -> stringResource(R.string.friday)
    6 -> stringResource(R.string.saturday)
    7 -> stringResource(R.string.sunday)
    else -> stringResource(R.string.unknown_day)
}

@Composable
fun CreatePlanScreen(viewModel: PlansViewModel, onCreated: (String) -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var day by rememberSaveable { mutableStateOf(1) }
    var dayMenuOpen by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.create_plan), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; submitted = false },
            label = { Text(stringResource(R.string.plan_name)) },
            isError = submitted && name.isBlank(),
            supportingText = if (submitted && name.isBlank()) {{ Text(stringResource(R.string.plan_name_required)) }} else null,
            modifier = Modifier.fillMaxWidth()
        )
        Box {
            OutlinedButton(onClick = { dayMenuOpen = true }) {
                Text(stringResource(R.string.training_day, dayName(day)))
            }
            DropdownMenu(expanded = dayMenuOpen, onDismissRequest = { dayMenuOpen = false }) {
                (1..7).forEach { option ->
                    DropdownMenuItem(text = { Text(dayName(option)) }, onClick = {
                        day = option
                        dayMenuOpen = false
                    })
                }
            }
        }
        Button(onClick = {
            submitted = true
            val cleanName = name.trim()
            if (cleanName.isNotEmpty()) onCreated(viewModel.createPlan(cleanName, day).id)
        }) { Text(stringResource(R.string.create_plan)) }
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}
