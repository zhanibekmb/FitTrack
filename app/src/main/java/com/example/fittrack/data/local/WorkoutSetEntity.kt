package com.example.fittrack.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "workout_sets",
    primaryKeys = ["sessionId", "exerciseId", "setNumber"],
    foreignKeys = [ForeignKey(
        entity = WorkoutSessionEntity::class,
        parentColumns = ["id"], childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class WorkoutSetEntity(
    val sessionId: String,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val position: Int
)
