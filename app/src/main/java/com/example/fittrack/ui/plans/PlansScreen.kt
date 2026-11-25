package com.example.fittrack.ui.plans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.ui.components.EmptyState

@Composable
fun PlansScreen(viewModel: PlansViewModel, onOpenPlan: (String) -> Unit, onCreatePlan: () -> Unit) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val plans = data.plans
    var query by rememberSaveable { mutableStateOf("") }
    val visiblePlans = plans.filter { it.name.contains(query.trim(), ignoreCase = true) }
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Button(onClick = onCreatePlan, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.create_plan)) } }
        item {
            OutlinedTextField(value = query, onValueChange = { query = it },
                label = { Text(stringResource(R.string.search_plans)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = if (query.isNotEmpty()) {{
                    TextButton(onClick = { query = "" }) { Text(stringResource(R.string.clear_search)) }
                }} else null)
        }
        if (plans.isEmpty()) item {
            EmptyState(stringResource(R.string.no_plans), stringResource(R.string.create_plan))
        } else if (visiblePlans.isEmpty()) item { Text(stringResource(R.string.no_matching_plans)) }
        items(visiblePlans, key = { it.id }) { plan ->
            Card(onClick = { onOpenPlan(plan.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan.name, style = MaterialTheme.typography.titleLarge)
                    Text(dayName(plan.dayOfWeek), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(pluralStringResource(R.plurals.plans_exercise_count, plan.exercises.size, plan.exercises.size),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun dayName(dayOfWeek: Int): String = stringResource(when (dayOfWeek) {
    1 -> R.string.monday
    2 -> R.string.tuesday
    3 -> R.string.wednesday
    4 -> R.string.thursday
    5 -> R.string.friday
    6 -> R.string.saturday
    7 -> R.string.sunday
    else -> R.string.unknown_day
})

@Composable
fun PlanFormScreen(
    viewModel: PlansViewModel,
    onSaved: (String) -> Unit,
    plan: WorkoutPlan? = null
) {
    val action by viewModel.action.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf(plan?.name.orEmpty()) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var day by rememberSaveable { mutableIntStateOf(plan?.dayOfWeek ?: 1) }
    var dayMenuOpen by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (action.failed) Text(stringResource(R.string.storage_save_failed), color = MaterialTheme.colorScheme.error)
        OutlinedTextField(value = name, onValueChange = {
                name = it
                submitted = false
            },
            label = { Text(stringResource(R.string.plan_name)) }, singleLine = true,
            isError = submitted && name.isBlank(),
            supportingText = if (submitted && name.isBlank()) {{ Text(stringResource(R.string.plan_name_required)) }} else null,
            modifier = Modifier.fillMaxWidth())
        Box {
            OutlinedButton(onClick = { dayMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.training_day, dayName(day)))
            }
            DropdownMenu(expanded = dayMenuOpen, onDismissRequest = { dayMenuOpen = false }) {
                for (option in 1..7) {
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
            if (cleanName.isNotEmpty()) {
                if (plan == null) {
                    viewModel.createPlan(cleanName, day, onSaved)
                } else {
                    viewModel.editPlan(plan.id, cleanName, day) { onSaved(plan.id) }
                }
            }
        }, enabled = !action.saving, modifier = Modifier.fillMaxWidth()) {
            val label = if (plan == null) R.string.create_plan else R.string.save
            Text(stringResource(label))
        }
    }
}
