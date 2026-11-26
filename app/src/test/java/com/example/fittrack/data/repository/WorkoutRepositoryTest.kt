
package com.example.fittrack.data.repository

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.test.runTest

class WorkoutRepositoryTest {

    @Test
    fun repositoryStartsWithDemoData() = runTest {
        val repository = WorkoutRepository()

        assertEquals(3, repository.data.value.plans.size)
        assertEquals(9, repository.data.value.exercises.size)
        assertTrue(repository.data.value.sessions.isEmpty())
        assertNull(repository.data.value.activeSession)
    }

    @Test
    fun createPlanAndAddExercise() = runTest {
        val repository = WorkoutRepository()

        val plan = repository.createPlan(
            name = "Upper Body",
            dayOfWeek = 2
        )

        assertEquals("Upper Body", plan.name)
        assertEquals(2, plan.dayOfWeek)

        val added = repository.addExerciseToPlan(
            planId = plan.id,
            exerciseId = "db-bench",
            targetSets = 3,
            targetReps = 12,
            targetWeightKg = 15.0
        )

        assertTrue(added)

        val savedPlan = repository.data.value.plans
            .first { it.id == plan.id }

        assertEquals(1, savedPlan.exercises.size)

        val exercise = savedPlan.exercises.first()

        assertEquals("db-bench", exercise.exerciseId)
        assertEquals(3, exercise.targetSets)
        assertEquals(12, exercise.targetReps)
        assertEquals(15.0, exercise.targetWeightKg, 0.001)
    }

    @Test
    fun startWorkoutCreatesActiveSession() = runTest {
        val repository = WorkoutRepository()

        val session = repository.startWorkout("push-day")

        assertNotNull(session)
        assertEquals("push-day", session?.planId)
        assertNotNull(repository.data.value.activeSession)
        assertTrue(repository.data.value.sessions.isEmpty())

        // Cannot start another workout while active
        val second = repository.startWorkout("leg-day")

        assertNull(second)
    }

    @Test
    fun completeSetValidatesInput() = runTest {
        val repository = WorkoutRepository()

        repository.startWorkout("push-day")

        // Invalid exercise for Push Day
        assertFalse(
            repository.completeSet("squat", 1, 40.0, 10)
        )

        // Invalid repetition count
        assertFalse(
            repository.completeSet("db-bench", 1, 15.0, 0)
        )

        // Invalid weight
        assertFalse(
            repository.completeSet("db-bench", 1, -5.0, 12)
        )

        // Valid set
        assertTrue(
            repository.completeSet("db-bench", 1, 15.0, 12)
        )

        // Duplicate set number
        assertFalse(
            repository.completeSet("db-bench", 1, 15.0, 12)
        )

        assertEquals(
            1,
            repository.data.value.activeSession
                ?.completedSets?.size
        )
    }

    @Test
    fun finishWorkoutSavesHistory() = runTest {
        val repository = WorkoutRepository()

        repository.startWorkout("push-day")

        repository.completeSet(
            "db-bench", 1, 15.0, 12
        )

        repository.completeSet(
            "db-bench", 2, 15.0, 10
        )

        val finished = repository.finishWorkout()

        assertNotNull(finished)
        assertTrue(finished!!.isFinished)
        assertEquals(2, finished.completedSets.size)

        // Workout moved to history
        assertEquals(1, repository.data.value.sessions.size)

        // No active workout
        assertNull(repository.data.value.activeSession)

        // Volume: 15 * 12 = 180
        val firstSet = finished.completedSets.first()

        assertEquals(180.0, firstSet.volumeKg, 0.001)
    }

    @Test
    fun previousSetsReturnsLatestResults() = runTest {
        val repository = WorkoutRepository()

        // First workout
        repository.startWorkout("push-day")
        repository.completeSet("db-bench", 1, 15.0, 12)
        repository.finishWorkout()

        // Second workout
        repository.startWorkout("push-day")
        repository.completeSet("db-bench", 1, 17.5, 10)
        repository.finishWorkout()

        val previous = repository.getPreviousSets(
            "db-bench"
        )

        assertEquals(1, previous.size)
        assertEquals(17.5, previous[0].weightKg, 0.001)
        assertEquals(10, previous[0].reps)
    }

    @Test
    fun editAndDeletePlanKeepFinishedWorkoutHistory() = runTest {
        val repository = WorkoutRepository()
        val plan = repository.createPlan("Upper Body", 2)
        assertNull(repository.startWorkout(plan.id))
        assertFalse(repository.editPlan(plan.id, " ", 4))
        assertFalse(repository.editPlan(plan.id, "Strength", 8))
        assertTrue(repository.addExerciseToPlan(plan.id, "db-bench", 3, 10, 15.0))
        assertTrue(repository.editPlan(plan.id, " Strength ", 4))

        val editedPlan = repository.data.value.plans.first { it.id == plan.id }
        assertEquals("Strength", editedPlan.name)
        assertEquals(4, editedPlan.dayOfWeek)
        assertEquals(1, editedPlan.exercises.size)

        assertNotNull(repository.startWorkout(plan.id))
        assertFalse(repository.deletePlan(plan.id))
        assertTrue(repository.completeSet("db-bench", 1, 15.0, 10))
        val finished = repository.finishWorkout()
        assertNull(repository.data.value.activeSession)
        assertTrue(repository.deletePlan(plan.id))
        assertFalse(repository.data.value.plans.any { it.id == plan.id })
        assertEquals(listOf(finished), repository.data.value.sessions)
        assertEquals("Strength", finished?.planName)
    }

    @Test
    fun cannotFinishEmptyWorkout() = runTest {
        val repository = WorkoutRepository()

        repository.startWorkout("push-day")

        val result = repository.finishWorkout()

        assertNull(result)
        assertNotNull(repository.data.value.activeSession)
        assertTrue(repository.data.value.sessions.isEmpty())
    }
}
