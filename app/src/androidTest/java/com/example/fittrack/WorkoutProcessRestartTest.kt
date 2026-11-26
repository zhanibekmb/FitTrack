package com.example.fittrack

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fittrack.data.repository.FitTrackContainer
import com.example.fittrack.data.repository.WorkoutPlanRepository
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.CatalogCredit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

/** Run both phases using scripts/check-workout-restart.ps1; each phase uses a new app process. */
class WorkoutProcessRestartTest {
    @Test fun persistedPlanAndResults() = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        val phase = args.getString("persistencePhase")
        val context = ApplicationProvider.getApplicationContext<Context>()
        FitTrackContainer.initialize(context)
        val repository = FitTrackContainer.repository
        withTimeout(10_000) { repository.data.first { !it.isLoading } }
        assertFalse(repository.data.value.storageError)
        if (phase == null) {
            val saved = WorkoutPlanRepository(FitTrackContainer.database(context)).read()
            assertEquals(saved.plans, repository.data.value.plans)
            assertEquals(saved.sessions, repository.data.value.sessions)
            return@runBlocking
        }
        require(phase == "prepare" || phase == "verify")
        val name = requireNotNull(args.getString("persistenceName"))
        if (phase == "prepare") {
            val plan = repository.createPlan(name, 4)
            val exercise = CatalogExercise(73, "Imported bench press", listOf("Chest"), "Chest",
                listOf("Barbell", "Bench"), CatalogCredit("Bench", emptyList(), "https://wger.de/", null), null)
            assertTrue(repository.addCatalogExerciseToPlan(plan.id, exercise, 3, 9, 27.5))
            assertNotNull(repository.startWorkout(plan.id))
            assertTrue(repository.completeSet("wger:73", 1, 27.5, 9))
            assertNotNull(repository.finishWorkout())
        } else {
            val plan = repository.data.value.plans.single { it.name == name }
            assertEquals(4, plan.dayOfWeek)
            assertEquals(27.5, plan.exercises.single().targetWeightKg, 0.0)
            val exercise = repository.data.value.exercises.single { it.id == "wger:73" }
            assertEquals("Imported bench press", exercise.name)
            assertEquals(listOf("Barbell", "Bench"), exercise.equipment)
            val saved = repository.data.value.sessions.single { it.planId == plan.id }
            assertEquals(name, saved.planName)
            assertEquals(247.5, saved.totalVolumeKg(), 0.0)
            assertEquals(9, saved.completedSets.single().reps)
            assertNull(repository.data.value.activeSession)
        }
    }
}
