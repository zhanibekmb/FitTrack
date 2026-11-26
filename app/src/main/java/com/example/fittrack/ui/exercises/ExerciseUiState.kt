package com.example.fittrack.ui.exercises

import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.CatalogMuscle

data class ExerciseUiState(
    val query: String = "",
    val muscles: List<CatalogMuscle> = emptyList(),
    val muscleId: Int? = null,
    val content: ExerciseCatalogContent = ExerciseCatalogContent.Loading
)

sealed interface ExerciseCatalogContent {
    data object Loading : ExerciseCatalogContent
    data object Empty : ExerciseCatalogContent
    data class Error(val problem: ExerciseLoadProblem) : ExerciseCatalogContent
    data class Success(
        val exercises: List<CatalogExercise>,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
        val pageError: ExerciseLoadProblem? = null
    ) : ExerciseCatalogContent
}

enum class ExerciseLoadProblem {
    NETWORK, TIMEOUT, SERVICE, INVALID_RESPONSE
}
