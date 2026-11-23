package com.example.fittrack.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fittrack.R
import com.example.fittrack.data.model.ThemeMode

@Composable
fun SettingsScreen(uiState: SettingsUiState, onSelectTheme: (ThemeMode) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.selectableGroup()) {
                for (mode in ThemeMode.entries) {
                    val label = when (mode) {
                        ThemeMode.SYSTEM -> R.string.theme_system
                        ThemeMode.LIGHT -> R.string.theme_light
                        ThemeMode.DARK -> R.string.theme_dark
                    }
                    val selected = uiState.themeMode == mode
                    Row(
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp)
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onSelectTheme(mode) }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(stringResource(label), modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
        if (uiState.saveFailed) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.theme_save_failed),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
