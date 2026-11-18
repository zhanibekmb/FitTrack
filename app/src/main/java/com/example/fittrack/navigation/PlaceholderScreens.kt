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
            text = "My Workout Plans",
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
                        text = "${plan.exercises.size} exercises"
                    )

                    Button(
                        onClick = {
                            onStartWorkout(plan.id)
                        }
                    ) {
                        Text("Start Workout")
                    }
                }
            }
        }

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}



@Composable
fun PlansPlaceholderScreen(
    onStartWorkout: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("My Workout Plans")

        Button(
            onClick = {
                onStartWorkout("push-001")
            }
        ) {
            Text("Start Push Day")
        }

        Button(onClick = onBack) {
            Text("Back")
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
            text = "Workout History",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Here will be your workout history.")

        OutlinedButton(onClick = onBack) {
            Text("Back")
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
            text = "Active Workout",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Selected plan ID: $planId")

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}