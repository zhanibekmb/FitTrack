package com.example.fittrack.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class PlanWithExercises(
    @Embedded val plan: WorkoutPlanEntity,
    @Relation(parentColumn = "id", entityColumn = "planId")
    val exercises: List<PlannedExerciseEntity>
)

@Dao
abstract class WorkoutPlanDao {
    @Transaction
    @Query("SELECT * FROM workout_plans ORDER BY position, id")
    abstract fun observePlans(): Flow<List<PlanWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_plans ORDER BY position, id")
    abstract suspend fun getPlans(): List<PlanWithExercises>

    @Query("SELECT position FROM workout_plans WHERE id = :planId")
    abstract suspend fun getPosition(planId: String): Int?

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM workout_plans")
    abstract suspend fun nextPosition(): Int

    @Upsert
    abstract suspend fun upsertPlan(plan: WorkoutPlanEntity)

    @Insert
    abstract suspend fun insertExercises(exercises: List<PlannedExerciseEntity>)

    @Query("DELETE FROM planned_exercises WHERE planId = :planId")
    abstract suspend fun clearExercises(planId: String)

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    abstract suspend fun deletePlan(planId: String): Int

    @Query("SELECT COUNT(*) FROM local_metadata WHERE `key` = 'plans_seeded'")
    abstract suspend fun isSeeded(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun markSeeded(marker: LocalMetadataEntity)

    @Transaction
    open suspend fun savePlan(plan: WorkoutPlanEntity, exercises: List<PlannedExerciseEntity>) {
        upsertPlan(plan)
        clearExercises(plan.id)
        insertExercises(exercises)
    }
}
