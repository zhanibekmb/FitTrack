package com.example.fittrack.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_exercises")
data class CatalogExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val muscleGroup: String,
    val equipmentJson: String
)
