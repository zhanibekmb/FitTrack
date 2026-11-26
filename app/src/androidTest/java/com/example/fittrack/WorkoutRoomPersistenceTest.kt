package com.example.fittrack

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fittrack.data.local.*
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.repository.WorkoutPlanRepository
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class WorkoutRoomPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "persistence-${UUID.randomUUID()}.db"
    private var database: FitTrackDatabase? = null

    private fun open(): FitTrackDatabase = Room.databaseBuilder(context,
        FitTrackDatabase::class.java, name).build().also { database = it }

    @After fun cleanUp() {
        database?.close()
        context.deleteDatabase(name)
    }

    @Test fun planCrudAndFinishedSetsSurviveDatabaseReopen() = runBlocking {
        var db = open()
        var repository = WorkoutRepository(WorkoutPlanRepository(db))
        var observer = launch { repository.observeStorage() }
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        val plan = repository.createPlan("Persistent plan", 2)
        assertTrue(repository.addExerciseToPlan(plan.id, "db-bench", 2, 8, 17.5))
        assertTrue(repository.addExerciseToPlan(plan.id, "db-bench", 1, 6, 22.5))
        assertTrue(repository.editPlannedExercise(plan.id, 0, PlannedExercise("db-bench", 3, 10, 20.0)))
        assertTrue(repository.editPlan(plan.id, "Renamed plan", 5))
        observer.cancelAndJoin()
        db.close()

        // A new Room instance and repository have no access to the previous in-memory state.
        db = open()
        repository = WorkoutRepository(WorkoutPlanRepository(db))
        observer = launch { repository.observeStorage() }
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        val restored = repository.data.value.plans.single { it.id == plan.id }
        assertEquals("Renamed plan", restored.name)
        assertEquals(5, restored.dayOfWeek)
        assertEquals(listOf(20.0, 22.5), restored.exercises.map { it.targetWeightKg })
        assertEquals(listOf(3, 1), restored.exercises.map { it.targetSets })
        assertNotNull(repository.startWorkout(plan.id))
        assertFalse(repository.deletePlan(plan.id))
        assertTrue(repository.completeSet("db-bench", 1, 25.0, 8))
        assertTrue(repository.completeSet("db-bench", 5, 17.5, 12))
        val finished = repository.finishWorkout()!!
        assertTrue(repository.deletePlan(plan.id))
        observer.cancelAndJoin()
        db.close()

        db = open()
        repository = WorkoutRepository(WorkoutPlanRepository(db))
        observer = launch { repository.observeStorage() }
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        assertFalse(repository.data.value.plans.any { it.id == plan.id })
        assertEquals(finished, repository.data.value.sessions.single())
        assertEquals(410.0, repository.data.value.sessions.single().totalVolumeKg(), 0.0)
        assertEquals(listOf(1, 5), repository.getPreviousSets("db-bench").map { it.setNumber })
        assertEquals("Renamed plan", repository.data.value.sessions.single().planName)
        assertNull(repository.data.value.activeSession)
        assertTrue(db.planDao().getPlans().none { it.plan.id == plan.id })
        observer.cancelAndJoin()
    }

    @Test fun exerciseRemovalAndDeletingAllPlansArePermanent() = runBlocking {
        var db = open()
        var repository = WorkoutRepository(WorkoutPlanRepository(db))
        var observer = launch { repository.observeStorage() }
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        assertTrue(repository.removePlannedExercise("push-day", 0))
        val remaining = repository.data.value.plans.first { it.id == "push-day" }.exercises
        observer.cancelAndJoin()
        db.close()
        db = open()
        repository = WorkoutRepository(WorkoutPlanRepository(db))
        observer = launch { repository.observeStorage() }
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        assertEquals(remaining, repository.data.value.plans.first { it.id == "push-day" }.exercises)
        repository.data.value.plans.forEach { assertTrue(repository.deletePlan(it.id)) }
        observer.cancelAndJoin()
        db.close()
        db = open()
        val local = WorkoutPlanRepository(db)
        local.initialize(com.example.fittrack.data.DemoData.plans)
        assertTrue(local.read().plans.isEmpty())
        assertTrue(db.openHelper.readableDatabase.query("SELECT * FROM planned_exercises").use { it.count == 0 })
    }

    @Test fun duplicateSetRollsBackEntireFinishTransaction() = runBlocking {
        val db = open()
        val session = WorkoutSessionEntity("session", "plan", "Name", 100, 200)
        val set = WorkoutSetEntity("session", "db-bench", 1, 15.0, 10, 0)
        assertTrue(runCatching { db.historyDao().saveSession(session, listOf(set, set)) }.isFailure)
        assertTrue(db.historyDao().getSessions().isEmpty())
        assertTrue(db.openHelper.readableDatabase.query("SELECT * FROM workout_sets").use { it.count == 0 })
    }
}
