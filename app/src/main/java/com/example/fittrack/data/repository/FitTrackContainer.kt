package com.example.fittrack.data.repository

import android.content.Context
import androidx.room.Room
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object FitTrackContainer {
    val exerciseRepository by lazy { ExerciseRepository(RetrofitClient.createExerciseApi()) }
    private var workouts: WorkoutRepository? = null
    val repository: WorkoutRepository get() = checkNotNull(workouts) { "Initialize FitTrackContainer first" }
    private var databaseInstance: FitTrackDatabase? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observation: Job? = null
    private var settings: SettingsRepository? = null

    @Synchronized
    fun database(context: Context): FitTrackDatabase {
        return databaseInstance ?: Room.databaseBuilder(context.applicationContext,
            FitTrackDatabase::class.java, "fittrack.db")
            .addMigrations(FitTrackDatabase.MIGRATION_1_2).build().also { databaseInstance = it }
    }

    @Synchronized
    fun initialize(context: Context) {
        if (workouts == null) workouts = WorkoutRepository(WorkoutPlanRepository(database(context)))
        retryStorage()
    }

    fun retryStorage() {
        if (observation?.isActive == true) return
        observation = applicationScope.launch { repository.observeStorage() }
    }

    @Synchronized
    fun settingsRepository(context: Context): SettingsRepository {
        val existing = settings
        if (existing != null) return existing

        val repository = SettingsRepository(context.applicationContext)
        settings = repository
        return repository
    }
}
