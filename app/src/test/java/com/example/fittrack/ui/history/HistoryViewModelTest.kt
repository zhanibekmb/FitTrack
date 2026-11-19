package com.example.fittrack.ui.history

import com.example.fittrack.data.repository.WorkoutRepository
import org.junit.Assert.*
import org.junit.Test

class HistoryViewModelTest {
    @Test
    fun historyReflectsCompletedRepositoryWorkoutsWithoutSampleResults() {
        val repository = WorkoutRepository()
        val history = HistoryViewModel(repository)
        assertTrue(history.sessions.value.isEmpty())

        repository.startWorkout("push-day")
        repository.completeSet("db-bench", 1, 17.5, 8)
        assertTrue(history.sessions.value.isEmpty())

        val finished = repository.finishWorkout()
        assertEquals(listOf(finished), history.sessions.value)
        assertEquals(140.0, history.sessions.value.single().completedSets.single().volumeKg, 0.001)
        assertEquals("Dumbbell Bench Press", history.exercises.value.first { it.id == "db-bench" }.name)

        val plan = repository.createPlan("Upper Body", 2)
        assertTrue(history.plans.value.any { it.id == plan.id })
    }
}
