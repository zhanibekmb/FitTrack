package com.example.fittrack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "planned_exercises",
    primaryKeys = ["planId", "position"],
    foreignKeys = [ForeignKey(
        entity = WorkoutPlanEntity::class,
        parentColumns = ["id"], childColumns = ["planId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("planId")]
)
data class PlannedExerciseEntity(
    val planId: String,
    val position: Int,
    val exerciseId: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Double
)
