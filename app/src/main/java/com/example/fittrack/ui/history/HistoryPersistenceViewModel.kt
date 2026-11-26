package com.example.fittrack.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.local.HistoryPreferences
import com.example.fittrack.data.local.HistorySortOrder
import com.example.fittrack.data.local.SessionWithSets
import com.example.fittrack.data.local.WorkoutSessionEntity
import com.example.fittrack.data.remote.RemoteCatalogUiState
import com.example.fittrack.data.repository.HistoryPersistenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PersistedHistoryStats(
    val completedWorkoutsCount: Int = 0,
    val totalVolumeKg: Double = 0.0,
    val bestWeightKg: Double = 0.0
)

class HistoryPersistenceViewModel(
    private val repository: HistoryPersistenceRepository
) : ViewModel() {

    val sessions: StateFlow<List<SessionWithSets>> = repository.finishedSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val preferences: StateFlow<HistoryPreferences> = repository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryPreferences()
        )

    val stats: StateFlow<PersistedHistoryStats> = sessions
        .map { list ->
            val allSets = list.flatMap { it.sets }
            PersistedHistoryStats(
                completedWorkoutsCount = list.size,
                totalVolumeKg = allSets.sumOf { it.volumeKg },
                bestWeightKg = allSets.maxOfOrNull { it.weightKg } ?: 0.0
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PersistedHistoryStats()
        )

    private val _remoteCatalogState = MutableStateFlow<RemoteCatalogUiState>(RemoteCatalogUiState.Loading)
    val remoteCatalogState: StateFlow<RemoteCatalogUiState> = _remoteCatalogState.asStateFlow()

    init {
        loadRemoteExercises()
    }

    fun loadRemoteExercises() {
        viewModelScope.launch {
            _remoteCatalogState.value = RemoteCatalogUiState.Loading
            repository.fetchRemoteExercises()
                .onSuccess { items ->
                    _remoteCatalogState.value = if (items.isEmpty()) {
                        RemoteCatalogUiState.Empty
                    } else {
                        RemoteCatalogUiState.Success(items)
                    }
                }
                .onFailure { throwable ->
                    _remoteCatalogState.value = RemoteCatalogUiState.Error(
                        message = throwable.localizedMessage ?: "Failed to load exercises from API"
                    )
                }
        }
    }

    fun addWorkoutRecord(
        planName: String,
        exerciseName: String,
        weightKg: Double,
        reps: Int,
        setsCount: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addSampleOrCompletedWorkout(
                planName = planName,
                exerciseName = exerciseName,
                weightKg = weightKg,
                reps = reps,
                setsCount = setsCount,
                notes = notes
            )
        }
    }

    fun updateWorkoutRecord(session: WorkoutSessionEntity, newTitle: String, newNotes: String) {
        viewModelScope.launch {
            repository.updateWorkout(
                session.copy(
                    planName = newTitle.ifBlank { session.planName },
                    notes = newNotes
                )
            )
        }
    }

    fun deleteWorkoutRecord(sessionId: String) {
        viewModelScope.launch {
            repository.deleteWorkout(sessionId)
        }
    }

    fun toggleSortOrder() {
        viewModelScope.launch {
            val next = when (preferences.value.sortOrder) {
                HistorySortOrder.NEWEST_FIRST -> HistorySortOrder.OLDEST_FIRST
                HistorySortOrder.OLDEST_FIRST -> HistorySortOrder.NEWEST_FIRST
            }
            repository.updateSortOrder(next)
        }
    }

    fun toggleSetDetails() {
        viewModelScope.launch {
            repository.updateShowSetDetails(!preferences.value.showSetDetails)
        }
    }
}

class HistoryPersistenceViewModelFactory(
    private val repository: HistoryPersistenceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HistoryPersistenceViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return HistoryPersistenceViewModel(repository) as T
    }
}