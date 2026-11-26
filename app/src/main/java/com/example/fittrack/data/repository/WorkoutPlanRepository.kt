package com.example.fittrack.data.repository

import androidx.room.withTransaction
import com.example.fittrack.data.local.*
import com.example.fittrack.data.model.*
import kotlinx.coroutines.flow.combine
import com.example.fittrack.data.DemoData
import org.json.JSONArray

class WorkoutPlanRepository(private val database: FitTrackDatabase) : WorkoutStorage {
    private val plans = database.planDao()
    private val history = database.historyDao()

    override val changes = combine(plans.observePlans(), history.observeSessions(),
        plans.observeCatalogExercises()) { _, _, _ -> Unit }

    override suspend fun initialize(plans: List<WorkoutPlan>) {
        database.withTransaction {
            if (this@WorkoutPlanRepository.plans.isSeeded() == 0) {
                plans.forEach { plan -> savePlan(plan) }
                this@WorkoutPlanRepository.plans.markSeeded(LocalMetadataEntity("plans_seeded"))
            }
        }
    }

    override suspend fun read(): StoredWorkouts = database.withTransaction {
        StoredWorkouts(
            plans.getPlans().map { row ->
                WorkoutPlan(row.plan.id, row.plan.name, row.plan.dayOfWeek,
                    row.exercises.sortedBy { it.position }.map {
                        PlannedExercise(it.exerciseId, it.targetSets, it.targetReps, it.targetWeightKg)
                    })
            },
            history.getSessions().map { row ->
                WorkoutSession(row.session.id, row.session.planId, row.session.startedAt,
                    row.session.finishedAt,
                    row.sets.sortedBy { it.position }.map {
                        CompletedSet(it.exerciseId, it.setNumber, it.weightKg, it.reps)
                    }, row.session.planName)
            },
            DemoData.exercises + plans.getCatalogExercises().map { row ->
                val equipment = JSONArray(row.equipmentJson)
                Exercise(row.id, row.name, row.muscleGroup,
                    List(equipment.length()) { equipment.getString(it) })
            }.filterNot { cached -> DemoData.exercises.any { it.id == cached.id } }
        )
    }

    override suspend fun savePlan(plan: WorkoutPlan, importedExercise: Exercise?) {
        require(plan.name.isNotBlank() && plan.dayOfWeek in 1..7)
        require(plan.exercises.all {
            it.targetSets > 0 && it.targetReps > 0 &&
                it.targetWeightKg.isFinite() && it.targetWeightKg >= 0
        })
        database.withTransaction {
            importedExercise?.let {
                plans.cacheExercise(CatalogExerciseEntity(it.id, it.name, it.muscleGroup,
                    JSONArray(it.equipment).toString()))
            }
            val position = plans.getPosition(plan.id) ?: plans.nextPosition()
            plans.savePlan(WorkoutPlanEntity(plan.id, plan.name, plan.dayOfWeek, position),
            plan.exercises.mapIndexed { index, exercise ->
                PlannedExerciseEntity(plan.id, index, exercise.exerciseId,
                    exercise.targetSets, exercise.targetReps, exercise.targetWeightKg)
            })
        }
    }

    override suspend fun deletePlan(planId: String) { plans.deletePlan(planId) }

    override suspend fun saveSession(session: WorkoutSession) {
        require(session.isFinished && session.completedSets.isNotEmpty())
        require(session.completedSets.all {
            it.setNumber > 0 && it.reps > 0 && it.weightKg.isFinite() && it.weightKg >= 0
        })
        history.saveSession(
            WorkoutSessionEntity(session.id, session.planId, session.planName,
                session.startedAt, requireNotNull(session.finishedAt)),
            session.completedSets.mapIndexed { index, set ->
                WorkoutSetEntity(session.id, set.exerciseId, set.setNumber, set.weightKg, set.reps, index)
            })
    }
}
