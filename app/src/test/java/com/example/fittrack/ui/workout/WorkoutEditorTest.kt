package com.example.fittrack.ui.workout

import com.example.fittrack.data.DemoData
import com.example.fittrack.data.model.*
import org.junit.Assert.*
import org.junit.Test

class WorkoutEditorTest {
    @Test fun repeatedExercisesKeepDistinctTargetsAndSetNumbers() {
        val plan = WorkoutPlan("test", "Test", 1, listOf(
            PlannedExercise("db-bench", 2, 12, 15.0),
            PlannedExercise("db-bench", 1, 8, 20.0)
        ))
        val cards = workoutExercises(plan, DemoData.exercises, emptyList())
        assertEquals(1, cards.size)
        assertEquals(listOf(1, 2, 3), cards.single().sets.map { it.number })
        assertEquals(listOf("15", "15", "20"), cards.single().sets.map { it.weight })
        assertEquals(listOf("12", "12", "8"), cards.single().sets.map { it.reps })
    }

    @Test fun completedExtraSetsAreRestoredWithActualResults() {
        val saved = listOf(CompletedSet("db-bench", 5, 17.5, 7))
        val cards = workoutExercises(DemoData.plans.first(), DemoData.exercises, saved)
        val last = cards.first().sets.last()
        assertEquals(5, last.number)
        assertEquals("17.5", last.weight)
        assertEquals("7", last.reps)
        assertTrue(last.completed)
        assertFalse(cards.first().sets.first().completed)
    }

    @Test fun weightAcceptsBodyweightAndDecimalCommaButRejectsInvalidNumbers() {
        assertEquals(0.0, parsedWeight("0")!!, 0.0)
        assertEquals(12.5, parsedWeight(" 12,5 ")!!, 0.0)
        listOf("", "-1", "NaN", "Infinity", "1e999", "abc").forEach { assertNull(parsedWeight(it)) }
    }

    @Test fun repetitionsMustBePositiveWholeNumbers() {
        assertEquals(12, parsedReps(" 12 "))
        listOf("", "0", "-1", "2.5", "2147483648", "abc").forEach { assertNull(parsedReps(it)) }
    }
}
