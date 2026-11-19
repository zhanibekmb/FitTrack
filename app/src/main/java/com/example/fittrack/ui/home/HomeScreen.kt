package com.example.fittrack.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.fittrack.R
import com.example.fittrack.ui.components.StatCard
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenPlans: () -> Unit,
    onOpenHistory: () -> Unit,
    onResumeWorkout: (String) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.titleMedium
        )

        // STATISTICS
        Text(
            text = stringResource(R.string.statistics),
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatCard(
                title = stringResource(R.string.plans_count),
                value = uiState.totalPlans.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = stringResource(R.string.workouts_count),
                value = uiState.completedWorkouts.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        StatCard(
            title = stringResource(R.string.total_volume),
            value = stringResource(R.string.volume_value, String.format(
                LocalConfiguration.current.locales[0],
                "%.1f",
                uiState.totalVolumeKg
            )),
            modifier = Modifier.fillMaxWidth()
        )

        // ACTIVE WORKOUT
        if (uiState.activePlanId != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active_workout),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = uiState.activePlanName ?: stringResource(R.string.workout)
                    )
                    Button(onClick = { onResumeWorkout(uiState.activePlanId) }) {
                        Text(stringResource(R.string.resume_workout))
                    }
                }
            }
        }

        // TODAY'S WORKOUT
        Text(
            text = stringResource(R.string.today_workout),
            style = MaterialTheme.typography.titleLarge
        )

        if (uiState.todayPlans.isEmpty()) {
            Text(stringResource(R.string.rest_day))
        } else {
            uiState.todayPlans.forEach { plan ->

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
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = stringResource(R.string.exercise_count, plan.exercises.size)
                        )

                        Button(onClick = onOpenPlans) {
                            Text(stringResource(R.string.open_plans))
                        }
                    }
                }
            }
        }

        // NAVIGATION BUTTONS
        Button(
            onClick = onOpenPlans,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.workout_plans))
        }

        OutlinedButton(
            onClick = onOpenHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.workout_history))
        }
    }
}
