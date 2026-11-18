package com.example.fittrack.data.model

data class CompletedSet(
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int
) {
    val volumeKg: Double
        get() = weightKg * reps
}
