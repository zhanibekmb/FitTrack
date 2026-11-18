package com.example.fittrack.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderScreen(
    onStartWorkout: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Workout Plans",
            style = MaterialTheme.typography.headlineMedium
        )
        Text("Example program: Push Day")

        Button(
            onClick = { onStartWorkout("push-001") }
        ) {
            Text("Start Push Day")
        }

        OutlinedButton(onClick = onBack) {
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