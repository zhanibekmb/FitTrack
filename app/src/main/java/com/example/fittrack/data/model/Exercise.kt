package com.example.fittrack.data.model

data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: List<String> = emptyList()
)
