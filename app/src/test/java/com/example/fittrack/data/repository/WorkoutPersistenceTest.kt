package com.example.fittrack.data.repository

import com.example.fittrack.data.model.WorkoutPlan
import com.example.fittrack.data.model.WorkoutSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutPersistenceTest {
    private class Storage : WorkoutStorage {
        private val revision = MutableStateFlow(0)
        override val changes = revision.map { Unit }
        var saved = StoredWorkouts(emptyList(), emptyList())
        var fail = false
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun initialize(plans: List<WorkoutPlan>) { saved = saved.copy(plans = plans) }
        override suspend fun read() = saved
        override suspend fun savePlan(plan: WorkoutPlan) {
            if (fail) throw IOException("Disk unavailable")
            saved = saved.copy(plans = saved.plans.filterNot { it.id == plan.id } + plan)
            revision.value++
        }
        override suspend fun deletePlan(planId: String) {
            if (fail) throw IOException("Disk unavailable")
            saved = saved.copy(plans = saved.plans.filterNot { it.id == planId })
            revision.value++
        }
        override suspend fun saveSession(session: WorkoutSession) {
            gate?.await()
            if (fail) throw IOException("Disk unavailable")
            saved = saved.copy(sessions = saved.sessions + session)
            revision.value++
        }
    }

    @Test fun failedWritesKeepPlansAndCompletedWorkoutForRetry() = runTest {
        val storage = Storage()
        val repository = WorkoutRepository(storage)
        backgroundScope.launch { repository.observeStorage() }
        runCurrent()
        val plans = repository.data.value.plans
        storage.fail = true
        assertTrue(runCatching { repository.createPlan("Unsaved", 1) }.isFailure)
        assertTrue(runCatching { repository.editPlan("push-day", "Unsaved", 2) }.isFailure)
        assertTrue(runCatching { repository.deletePlan("push-day") }.isFailure)
        assertEquals(plans, repository.data.value.plans)
        repository.startWorkout("push-day")
        repository.completeSet("db-bench", 1, 17.5, 8)
        assertTrue(runCatching { repository.finishWorkout() }.isFailure)
        assertNotNull(repository.data.value.activeSession)
        assertFalse(repository.data.value.isSavingWorkout)
        assertTrue(repository.data.value.sessions.isEmpty())
        storage.fail = false
        assertNotNull(repository.finishWorkout())
        runCurrent()
        assertEquals(1, storage.saved.sessions.size)
        assertEquals(1, repository.data.value.sessions.size)
        assertNull(repository.data.value.activeSession)
    }

    @Test fun doubleFinishAndEditsDuringSavingCannotChangeStoredResults() = runTest {
        val storage = Storage()
        val repository = WorkoutRepository(storage)
        backgroundScope.launch { repository.observeStorage() }
        runCurrent()
        repository.startWorkout("push-day")
        repository.completeSet("db-bench", 1, 20.0, 10)
        storage.gate = CompletableDeferred()
        val first = async { repository.finishWorkout() }
        runCurrent()
        assertTrue(repository.data.value.isSavingWorkout)
        assertFalse(repository.removeCompletedSet("db-bench", 1))
        assertFalse(repository.completeSet("db-bench", 2, 99.0, 99))
        assertFalse(repository.discardWorkout())
        val second = async { repository.finishWorkout() }
        storage.gate!!.complete(Unit)
        assertNotNull(first.await())
        assertNull(second.await())
        runCurrent()
        assertEquals(1, storage.saved.sessions.size)
        assertEquals(200.0, storage.saved.sessions.single().totalVolumeKg(), 0.0)
    }
}
