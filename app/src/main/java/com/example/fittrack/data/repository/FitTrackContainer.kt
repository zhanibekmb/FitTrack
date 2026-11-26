package com.example.fittrack.data.repository

import android.content.Context
import com.example.fittrack.data.remote.RetrofitClient

object FitTrackContainer {
    val repository = WorkoutRepository()
    val exerciseRepository by lazy { ExerciseRepository(RetrofitClient.createExerciseApi()) }
    private var settings: SettingsRepository? = null

    @Synchronized
    fun settingsRepository(context: Context): SettingsRepository {
        val existing = settings
        if (existing != null) return existing

        val repository = SettingsRepository(context.applicationContext)
        settings = repository
        return repository
    }
}
