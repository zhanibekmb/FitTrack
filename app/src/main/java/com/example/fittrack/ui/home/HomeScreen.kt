package com.example.fittrack.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    var showDescription by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
        Text(
            text = "Plan your workouts and track your progress."
        )

        Button(
            onClick = { showDescription = !showDescription }
        ) {
            Text(
                text = if (showDescription) "Hide Description" else "How it works?"
            )
        }

        if (showDescription){
            Text(
                text = "Create programs, log your sets, " +
                        "and compare your workout results."
            )
        }
    }
}