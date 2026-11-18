package com.example.fittrack.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenPlans: () -> Unit,
    onOpenHistory: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "FitTrack",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Your workouts. Your progress.",
            style = MaterialTheme.typography.titleMedium
        )

        // STATISTICS
        Text(
            text = "My Statistics",
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatCard(
                title = "Plans",
                value = uiState.totalPlans.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Workouts",
                value = uiState.completedWorkouts.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        StatCard(
            title = "Total Volume",
            value = String.format(
                Locale.getDefault(),
                "%.1f kg",
                uiState.totalVolumeKg
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // ACTIVE WORKOUT
        if (uiState.activePlanName != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Active Workout",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = uiState.activePlanName
                    )
                }
            }
        }

        // TODAY'S WORKOUT
        Text(
            text = "Today's Workout",
            style = MaterialTheme.typography.titleLarge
        )

        if (uiState.todayPlans.isEmpty()) {
            Text("Rest day scheduled for today")
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
                            text = "${plan.exercises.size} exercises"
                        )

                        Button(onClick = onOpenPlans) {
                            Text("Open Plans")
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
            Text("My Workout Plans")
        }

        OutlinedButton(
            onClick = onOpenHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Workout History")
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(modifier = modifier) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}