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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fittrack.R
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.CatalogMuscle

@Composable
fun ExerciseCatalogScreen(
    state: ExerciseUiState,
    onSearch: (String) -> Unit,
    onSelectMuscle: (Int?) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onSelectExercise: (CatalogExercise) -> Unit
) {
    val focus = LocalFocusManager.current
    Column(Modifier.fillMaxSize().imePadding()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onSearch,
            label = { Text(stringResource(R.string.exercise_catalog_search)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            trailingIcon = if (state.query.isNotEmpty()) {
                {
                    TextButton(onClick = { onSearch("") }) {
                        Text(stringResource(R.string.clear_search))
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
        )
        if (state.muscles.isNotEmpty()) {
            MuscleFilter(state.muscles, state.muscleId, onSelectMuscle)
        }
        when (val content = state.content) {
            ExerciseCatalogContent.Loading -> CatalogStatus(Modifier.weight(1f)) {
                CatalogLoading()
            }
            ExerciseCatalogContent.Empty -> CatalogStatus(Modifier.weight(1f)) {
                val message = when {
                    state.query.isNotBlank() -> R.string.exercise_catalog_no_matches
                    state.muscleId != null -> R.string.exercise_catalog_no_muscle_matches
                    else -> R.string.exercise_catalog_empty
                }
                Text(stringResource(message), textAlign = TextAlign.Center)
            }
            is ExerciseCatalogContent.Error -> CatalogStatus(Modifier.weight(1f)) {
                CatalogError(content.problem, onRetry)
            }
            is ExerciseCatalogContent.Success -> LazyColumn(
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
                        CatalogStatus(Modifier) {
                            when {
                                content.loadingMore -> CatalogLoading()
                                content.pageError != null -> CatalogError(content.pageError, onRetry)
                                else -> OutlinedButton(
                                    onClick = onLoadMore,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.exercise_catalog_more)) }
                            }
                        }
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

@Composable
private fun CatalogStatus(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { content() }
}

@Composable
private fun CatalogLoading() {
    CircularProgressIndicator()
    Text(stringResource(R.string.exercise_catalog_loading))
}

@Composable
private fun CatalogError(problem: ExerciseLoadProblem, onRetry: () -> Unit) {
    val message = when (problem) {
        ExerciseLoadProblem.NETWORK -> R.string.exercise_catalog_network_error
        ExerciseLoadProblem.TIMEOUT -> R.string.exercise_catalog_timeout
        ExerciseLoadProblem.SERVICE -> R.string.exercise_catalog_service_error
        ExerciseLoadProblem.INVALID_RESPONSE -> R.string.exercise_catalog_invalid_response
    }
    Text(stringResource(message), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.exercise_catalog_retry)) }
}
