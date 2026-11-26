package com.example.fittrack.data.repository

import com.example.fittrack.data.local.HistoryPreferences
import com.example.fittrack.data.local.HistoryPreferencesDataStore
import com.example.fittrack.data.local.HistorySortOrder
import com.example.fittrack.data.local.SessionWithSets
import com.example.fittrack.data.local.WorkoutHistoryDao
import com.example.fittrack.data.local.WorkoutSessionEntity
import com.example.fittrack.data.local.WorkoutSetEntity
import com.example.fittrack.data.remote.RemoteExerciseItem
import com.example.fittrack.data.remote.WgerApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import java.util.UUID

class HistoryPersistenceRepository(
    private val dao: WorkoutHistoryDao,
    private val preferencesStore: HistoryPreferencesDataStore,
    private val apiService: WgerApiService
) {
    val preferences: Flow<HistoryPreferences> = preferencesStore.preferences

    @OptIn(ExperimentalCoroutinesApi::class)
    val finishedSessions: Flow<List<SessionWithSets>> =
        preferencesStore.preferences.flatMapLatest { prefs ->
            when (prefs.sortOrder) {
                HistorySortOrder.NEWEST_FIRST -> dao.observeFinishedSessionsNewestFirst()
                HistorySortOrder.OLDEST_FIRST -> dao.observeFinishedSessionsOldestFirst()
            }
        }

    fun observeExerciseSets(exerciseId: String): Flow<List<WorkoutSetEntity>> =
        dao.observeCompletedSetsForExercise(exerciseId)

    suspend fun addSampleOrCompletedWorkout(
        planName: String,
        exerciseName: String,
        weightKg: Double,
        reps: Int,
        setsCount: Int = 3,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()
        val exerciseId = exerciseName.trim().lowercase().replace("\\s+".toRegex(), "_")

        val session = WorkoutSessionEntity(
            id = sessionId,
            planId = null,
            planName = planName.ifBlank { "Workout" },
            startedAt = now - 30 * 60 * 1000L,
            finishedAt = now,
            isFinished = true,
            notes = notes
        )
        val sets = (1..setsCount).map { setNum ->
            WorkoutSetEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseName = exerciseName.ifBlank { "Exercise" },
                setNumber = setNum,
                weightKg = weightKg,
                reps = reps,
                completed = true
            )
        }
        dao.insertSessionWithSets(session, sets)
    }

    suspend fun updateWorkout(session: WorkoutSessionEntity) = withContext(Dispatchers.IO) {
        dao.updateSession(session)
    }

    suspend fun deleteWorkout(sessionId: String) = withContext(Dispatchers.IO) {
        dao.deleteSessionById(sessionId)
    }

    suspend fun updateSortOrder(order: HistorySortOrder) {
        preferencesStore.setSortOrder(order)
    }

    suspend fun updateShowSetDetails(show: Boolean) {
        preferencesStore.setShowSetDetails(show)
    }

    suspend fun fetchRemoteExercises(): Result<List<RemoteExerciseItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getExercises()
            response.results
                .filter { !it.name.isNullOrBlank() }
                .map { dto ->
                    val cleanText = dto.description
                        ?.replace("<[^>]*>".toRegex(), " ")
                        ?.replace("\\s+".toRegex(), " ")
                        ?.trim()
                        .orEmpty()
                        .ifBlank { "External exercise reference" }

                    RemoteExerciseItem(
                        id = dto.id,
                        name = dto.name!!.trim(),
                        summary = cleanText,
                        imageUrl = null
                    )
                }
        }
    }
}