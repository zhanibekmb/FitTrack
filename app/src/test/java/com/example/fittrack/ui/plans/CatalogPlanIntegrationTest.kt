package com.example.fittrack.ui.plans

import com.example.fittrack.data.model.CatalogCredit
import com.example.fittrack.data.model.CatalogExercise
import com.example.fittrack.data.model.PlannedExercise
import com.example.fittrack.data.repository.WorkoutRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogPlanIntegrationTest {
    private val repository = WorkoutRepository()
    private val plans = PlansViewModel(repository)
    private val bench = CatalogExercise(
        wgerId = 73,
        name = "Bench Press",
        muscles = listOf("Chest"),
        category = "Chest",
        equipment = listOf("Barbell", "Bench"),
        credit = CatalogCredit("Bench Press", emptyList(), "https://wger.de/en/exercise/73/view/", null),
        image = null
    )

    @Test
    fun importedExerciseRemainsAvailableToPlanWorkoutAndHistoryWithoutAnApiCall() {
        val plan = plans.createPlan("Strength", 2)
        val initialExercises = repository.data.value.exercises

        assertTrue(plans.addCatalogExercise(plan.id, bench, 2, 8, 35.5))
        assertTrue(plans.addCatalogExercise(plan.id, bench, 1, 10, 25.0))
        val added = repository.data.value
        assertEquals(initialExercises, added.exercises.filterNot { it.id == "wger:73" })
        val registered = added.exercises.single { it.id == "wger:73" }
        assertEquals("Bench Press", registered.name)
        assertEquals("Chest", registered.muscleGroup)
        assertEquals(listOf("Barbell", "Bench"), registered.equipment)
        assertEquals(
            listOf(PlannedExercise("wger:73", 2, 8, 35.5), PlannedExercise("wger:73", 1, 10, 25.0)),
            added.plans.single { it.id == plan.id }.exercises
        )

        assertNotNull(repository.startWorkout(plan.id))
        assertNull(repository.startWorkout("push-day"))
        assertFalse(plans.deletePlan(plan.id))
        assertTrue(repository.completeSet("wger:73", 1, 35.5, 8))
        val history = checkNotNull(repository.finishWorkout())
        assertEquals("wger:73", history.completedSets.single().exerciseId)
        assertEquals(registered, repository.data.value.exercises.single { it.id == "wger:73" })
        assertEquals(history.completedSets, repository.getPreviousSets("wger:73"))
    }

    @Test
    fun invalidTargetsOrMissingPlanDoNotRegisterMetadata() {
        val plan = plans.createPlan("Strength", 2)
        val before = repository.data.value

        assertFalse(plans.addCatalogExercise(plan.id, bench, 0, 8, 35.5))
        assertFalse(plans.addCatalogExercise(plan.id, bench, 2, 0, 35.5))
        assertFalse(plans.addCatalogExercise(plan.id, bench, 2, 8, Double.NaN))
        assertFalse(plans.addCatalogExercise("missing-plan", bench, 2, 8, 35.5))
        assertFalse(plans.addExercise(plan.id, "wger:73", 2, 8, 35.5))
        assertSame(before, repository.data.value)
    }
}
