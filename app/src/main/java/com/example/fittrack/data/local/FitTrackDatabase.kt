package com.example.fittrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WorkoutPlanEntity::class, PlannedExerciseEntity::class,
        WorkoutSessionEntity::class, WorkoutSetEntity::class, LocalMetadataEntity::class,
        CatalogExerciseEntity::class],
    version = 2,
    exportSchema = true
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun planDao(): WorkoutPlanDao
    abstract fun historyDao(): WorkoutHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `catalog_exercises` (`id` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, `muscleGroup` TEXT NOT NULL, `equipmentJson` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }
    }
}
