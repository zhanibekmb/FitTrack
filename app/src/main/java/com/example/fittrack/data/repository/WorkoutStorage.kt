package com.example.fittrack.data.repository

import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession
import com.example.fittrack.data.model.Exercise
import com.example.fittrack.data.DemoData
import kotlinx.coroutines.flow.Flow

data class StoredWorkouts(
    val plans: List<WorkoutPlan>,
    val sessions: List<WorkoutSession>,
    val exercises: List<Exercise> = DemoData.exercises
)

interface WorkoutStorage {
    val changes: Flow<Unit>
    suspend fun initialize(plans: List<WorkoutPlan>)
    suspend fun read(): StoredWorkouts
    suspend fun savePlan(plan: WorkoutPlan, importedExercise: Exercise? = null)
    suspend fun deletePlan(planId: String)
    suspend fun saveSession(session: WorkoutSession)
}
