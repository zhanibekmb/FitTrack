package com.example.fittrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dayOfWeek: Int,
    val position: Int
)

@Entity(tableName = "local_metadata")
data class LocalMetadataEntity(@PrimaryKey val key: String)
