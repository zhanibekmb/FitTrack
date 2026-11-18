package com.example.fittrack.navigation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.fittrack.data.model.WorkoutPlan
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.fittrack.R
import androidx.compose.ui.unit.dp


@Composable
fun PlansPlaceholderScreen(
    plans: List<WorkoutPlan>,
    onStartWorkout: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = stringResource(R.string.workout_plans),
            style = MaterialTheme.typography.headlineMedium
        )

        plans.forEach { plan ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = stringResource(R.string.exercise_count, plan.exercises.size)
                    )

                    Button(
                        onClick = {
                            onStartWorkout(plan.id)
                        }
                    ) {
                        Text(stringResource(R.string.start_workout))
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}



@Composable
fun HistoryPlaceholderScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.workout_history),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(stringResource(R.string.history_pending))

        OutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun WorkoutPlaceholderScreen(
    planId: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.active_workout),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(stringResource(R.string.selected_plan, planId))
        Text(stringResource(R.string.workout_pending))

        OutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun InvalidPlanPlaceholderScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.invalid_plan))
        OutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}
