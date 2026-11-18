package com.example.fittrack.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fittrack.R

@Composable
fun RestTimerCard(
    state: RestTimerState,
    onDuration: (Int) -> Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit
) {
    var customSeconds by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.rest_timer), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.rest_duration, state.durationSeconds))
            Text(
                text = "%02d:%02d".format(state.remainingSeconds / 60, state.remainingSeconds % 60),
                style = MaterialTheme.typography.displaySmall
            )
            if (state.finished) Text(stringResource(R.string.rest_complete),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(60, 90, 120).forEach { seconds ->
                    TextButton(onClick = { onDuration(seconds) }) { Text(stringResource(R.string.rest_seconds, seconds)) }
                }
            }
            OutlinedTextField(
                value = customSeconds,
                onValueChange = { customSeconds = it; invalid = false },
                label = { Text(stringResource(R.string.rest_custom)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = invalid,
                supportingText = if (invalid) {{ Text(stringResource(R.string.rest_custom_error)) }} else null,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = {
                val seconds = customSeconds.trim().toIntOrNull()
                invalid = seconds == null || !onDuration(seconds)
            }) { Text(stringResource(R.string.rest_apply)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = if (state.running) onPause else onStart) {
                    Text(stringResource(if (state.running) R.string.rest_pause else R.string.rest_start))
                }
                OutlinedButton(onClick = onReset) { Text(stringResource(R.string.rest_reset)) }
            }
            TextButton(onClick = onSkip) { Text(stringResource(R.string.rest_skip)) }
        }
    }
}
