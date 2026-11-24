package com.example.fittrack.ui.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrack.R
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.CatalogMuscle

@Composable
fun ExerciseCatalogScreen(
    viewModel: ExerciseViewModel,
    onSelectExercise: (CatalogExercise) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxSize().imePadding()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::search,
            label = { Text(stringResource(R.string.exercise_catalog_search)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            trailingIcon = if (state.query.isNotEmpty()) {
                {
                    TextButton(onClick = { viewModel.search("") }) {
                        Text(stringResource(R.string.clear_search))
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
        )
        if (state.muscles.isNotEmpty()) {
            MuscleFilter(state.muscles, state.muscleId, viewModel::selectMuscle)
        }
        val content = state.content
        if (content is ExerciseCatalogContent.Success) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(content.exercises, key = { it.id }) { exercise ->
                    ExerciseCatalogItem(exercise) {
                        focus.clearFocus()
                        onSelectExercise(exercise)
                    }
                }
                if (content.hasMore) {
                    item {
                        OutlinedButton(
                            onClick = viewModel::loadMore,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.exercise_catalog_more)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MuscleFilter(muscles: List<CatalogMuscle>, selectedId: Int?, onSelect: (Int?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val allMuscles = stringResource(R.string.exercise_catalog_all_muscles)
    val selectedName = muscles.firstOrNull { it.id == selectedId }?.name ?: allMuscles
    Box(Modifier.padding(horizontal = 20.dp)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.exercise_catalog_filter, selectedName))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(allMuscles) }, onClick = {
                expanded = false
                onSelect(null)
            })
            for (muscle in muscles) {
                DropdownMenuItem(text = { Text(muscle.name) }, onClick = {
                    expanded = false
                    onSelect(muscle.id)
                })
            }
        }
    }
}
