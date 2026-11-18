package com.example.fittrack.data.repository

object FitTrackContainer {

    val repository: WorkoutRepository by lazy {
        WorkoutRepository()
    }
}