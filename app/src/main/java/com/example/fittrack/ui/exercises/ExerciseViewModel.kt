package com.example.fittrack.ui.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrack.data.repository.ExerciseRepository
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.data.repository.InvalidExerciseResponse
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException

class ExerciseViewModel(private val repository: ExerciseRepository) : ViewModel() {
    private val _state = MutableStateFlow(ExerciseUiState())
    val state = _state.asStateFlow()

    private var request: Job? = null
    private var muscleRequest: Job? = null
    private var nextOffset: Int? = null

    init {
        loadFirstPage()
        loadMuscles()
    }

    private fun loadMuscles() {
        if (_state.value.muscles.isNotEmpty() || muscleRequest?.isActive == true) return
        muscleRequest = viewModelScope.launch {
            try {
                val muscles = repository.loadMuscles()
                ensureActive()
                _state.update { it.copy(muscles = muscles) }
            } catch (failure: Exception) {
                ensureActive()
                // The filter menu is optional. Exercise requests still have their own error and retry.
                failure.asLoadProblem()
            }
        }
    }

    fun search(query: String) {
        val previous = _state.value.query
        if (query == previous) return
        if (query.trim() == previous.trim()) {
            _state.update { it.copy(query = query) }
            return
        }
        request?.cancel()
        _state.update { it.copy(query = query) }
        loadFirstPage(debounce = true)
    }

    fun selectMuscle(muscleId: Int?) {
        if (muscleId == _state.value.muscleId) return
        if (muscleId != null && _state.value.muscles.none { it.id == muscleId }) return
        request?.cancel()
        _state.update { it.copy(muscleId = muscleId) }
        loadFirstPage()
    }

    fun loadMore() {
        val content = _state.value.content as? ExerciseCatalogContent.Success ?: return
        if (!content.loadingMore && content.pageError == null) loadNextPage(content)
    }

    fun retry() {
        when (val content = _state.value.content) {
            is ExerciseCatalogContent.Error -> {
                loadFirstPage()
                loadMuscles()
            }
            is ExerciseCatalogContent.Success -> {
                if (!content.loadingMore && content.pageError != null) loadNextPage(content)
            }
            else -> Unit
        }
    }

    private fun loadFirstPage(debounce: Boolean = false) {
        nextOffset = null
        updateContent(ExerciseCatalogContent.Loading)
        loadPage(offset = 0, debounce = debounce)
    }

    private fun loadNextPage(content: ExerciseCatalogContent.Success) {
        val offset = nextOffset ?: return
        updateContent(content.copy(loadingMore = true, pageError = null))
        loadPage(offset = offset, previous = content)
    }

    private fun loadPage(
        offset: Int,
        debounce: Boolean = false,
        previous: ExerciseCatalogContent.Success? = null
    ) {
        val filters = _state.value
        request = viewModelScope.launch {
            if (debounce) delay(SEARCH_DELAY_MS)
            try {
                val page = repository.loadPage(filters.query, offset, filters.muscleId)
                ensureActive()
                nextOffset = page.nextOffset
                val exercises = (previous?.exercises.orEmpty() + page.exercises).distinctBy { it.id }
                val content = if (exercises.isEmpty()) {
                    ExerciseCatalogContent.Empty
                } else {
                    ExerciseCatalogContent.Success(exercises, hasMore = nextOffset != null)
                }
                updateContent(content)
            } catch (failure: Exception) {
                ensureActive()
                val problem = failure.asLoadProblem()
                updateContent(
                    previous?.copy(loadingMore = false, pageError = problem)
                        ?: ExerciseCatalogContent.Error(problem)
                )
            }
        }
    }

    private fun Exception.asLoadProblem(): ExerciseLoadProblem = when (this) {
        is InvalidExerciseResponse, is JsonParseException,
        is MalformedJsonException, is EOFException -> ExerciseLoadProblem.INVALID_RESPONSE
        is HttpException -> ExerciseLoadProblem.SERVICE
        is InterruptedIOException -> ExerciseLoadProblem.TIMEOUT
        is IOException -> ExerciseLoadProblem.NETWORK
        else -> throw this
    }

    private fun updateContent(content: ExerciseCatalogContent) {
        _state.update { it.copy(content = content) }
    }

    companion object {
        private const val SEARCH_DELAY_MS = 350L

        val Factory = viewModelFactory {
            initializer { ExerciseViewModel(FitTrackContainer.exerciseRepository) }
        }
    }
}
