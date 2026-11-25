package com.example.fittrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// History keeps its own name snapshot; deleting a plan must not delete its results.
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val planName: String?,
    val startedAt: Long,
    val finishedAt: Long
)
