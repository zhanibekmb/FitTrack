package com.example.fittrack.data.repository

import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

data class StoredWorkouts(val plans: List<WorkoutPlan>, val sessions: List<WorkoutSession>)

interface WorkoutStorage {
    val changes: Flow<Unit>
    suspend fun initialize(plans: List<WorkoutPlan>)
    suspend fun read(): StoredWorkouts
    suspend fun savePlan(plan: WorkoutPlan)
    suspend fun deletePlan(planId: String)
    suspend fun saveSession(session: WorkoutSession)
}
