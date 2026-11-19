package com.example.fittrack.ui.home

import com.example.fittrack.data.DemoData
import com.example.fittrack.data.model.CompletedSet
import com.example.fittrack.data.model.WorkoutSession
import org.junit.Assert.*
import org.junit.Test

class DashboardStateTest {
    @Test
    fun scheduleUsesMondayThroughSundayAndSupportsRestDays() {
        val monday = dashboardState(DemoData.plans, emptyList(), null, 1)
        assertEquals(listOf("push-day"), monday.todayPlans.map { it.id })
        assertEquals(3, monday.totalPlans)
        assertTrue(dashboardState(DemoData.plans, emptyList(), null, 7).todayPlans.isEmpty())
    }

    @Test
    fun statisticsIncludeOnlyFinishedResults() {
        val sets = listOf(CompletedSet("db-bench", 1, 15.0, 12))
        val finished = WorkoutSession("done", "push-day", 1L, 2L, sets)
        val active = WorkoutSession("active", "pull-day", 3L, completedSets = sets)
        val state = dashboardState(DemoData.plans, listOf(finished, active), active, 3)
        assertEquals(1, state.completedWorkouts)
        assertEquals(180.0, state.totalVolumeKg, 0.001)
        assertEquals("pull-day", state.activePlanId)
        assertEquals("Pull Day", state.activePlanName)
    }

    @Test
    fun missingActivePlanPreservesResumeIdWithoutInventingAName() {
        val active = WorkoutSession("active", "missing", 1L)
        val state = dashboardState(emptyList(), emptyList(), active, 1)
        assertEquals("missing", state.activePlanId)
        assertNull(state.activePlanName)
        assertEquals(0.0, state.totalVolumeKg, 0.001)
    }
}
