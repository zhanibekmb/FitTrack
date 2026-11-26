package com.example.fittrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {

    @Transaction //read
    @Query("SELECT * FROM workout_sessions WHERE isFinished = 1 ORDER BY finishedAt DESC, startedAt DESC")
    fun observeFinishedSessionsNewestFirst(): Flow<List<SessionWithSets>>

    @Transaction//
    @Query("SELECT * FROM workout_sessions WHERE isFinished = 1 ORDER BY finishedAt ASC, startedAt ASC")
    fun observeFinishedSessionsOldestFirst(): Flow<List<SessionWithSets>>

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND completed = 1")
    fun observeCompletedSetsForExercise(exerciseId: String): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Transaction//create
    suspend fun insertSessionWithSets(session: WorkoutSessionEntity, sets: List<WorkoutSetEntity>) {
        insertSession(session)
        insertSets(sets)
    }

    @Update//update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("UPDATE workout_sessions SET notes = :notes, planName = :planName WHERE id = :sessionId")
    suspend fun updateSessionDetails(sessionId: String, planName: String, notes: String)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")//delete
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAllSessions()
}