package com.example.fittrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutPlanEntity::class, PlannedExerciseEntity::class,
        WorkoutSessionEntity::class, WorkoutSetEntity::class, LocalMetadataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun planDao(): WorkoutPlanDao
    abstract fun historyDao(): WorkoutHistoryDao
}
