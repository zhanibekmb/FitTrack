package com.example.fittrack.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<WorkoutSetEntity>
)

@Dao
abstract class WorkoutHistoryDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY finishedAt, startedAt, id")
    abstract fun observeSessions(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY finishedAt, startedAt, id")
    abstract suspend fun getSessions(): List<SessionWithSets>

    @Insert
    abstract suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert
    abstract suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Transaction
    open suspend fun saveSession(session: WorkoutSessionEntity, sets: List<WorkoutSetEntity>) {
        insertSession(session)
        insertSets(sets)
    }
}
