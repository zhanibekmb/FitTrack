package com.example.fittrack.ui.workout

import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    private lateinit var dispatcher: TestDispatcher
    private val models = mutableListOf<WorkoutViewModel>()

    @Before fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun model(repository: WorkoutRepository) = WorkoutViewModel(repository) {
        dispatcher.scheduler.currentTime
    }.also { models.add(it) }

    @Test fun completeCorrectAndFinishStoresActualResultsWithoutChangingPlan() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        val original = repository.data.value.plans.first()
        repository.startWorkout(original.id)
        val vm = model(repository)
        runCurrent()
        vm.updateWeight("db-bench", 1, "17,5")
        vm.updateReps("db-bench", 1, "8")
        vm.setCompleted("db-bench", 1, true)
        vm.setCompleted("db-bench", 1, true)
        assertEquals(1, repository.data.value.activeSession!!.completedSets.size)
        assertTrue(vm.restTimer.value.running)
        vm.setCompleted("db-bench", 1, false)
        assertTrue(repository.data.value.activeSession!!.completedSets.isEmpty())
        vm.updateReps("db-bench", 1, "9")
        vm.setCompleted("db-bench", 1, true)
        vm.addSet("db-bench")
        vm.updateWeight("db-bench", 4, "10")
        vm.updateReps("db-bench", 4, "6")
        vm.setCompleted("db-bench", 4, true)
        assertTrue(vm.finishWorkout(original.id))
        runCurrent()
        val saved = repository.data.value.sessions.single()
        assertEquals(2, saved.completedSets.size)
        assertEquals(217.5, saved.completedSets.sumOf { it.volumeKg }, 0.001)
        assertEquals(original, repository.data.value.plans.first())
        assertNull(repository.data.value.activeSession)
        assertNull(vm.uiState.value.sessionId)
        assertFalse(vm.restTimer.value.running)
    }

    @Test fun invalidInputAndEmptyFinishDoNotSaveAnything() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        repository.startWorkout("push-day")
        val vm = model(repository)
        runCurrent()
        vm.updateWeight("db-bench", 1, "NaN")
        vm.updateReps("db-bench", 1, "0")
        vm.setCompleted("db-bench", 1, true)
        assertEquals(WorkoutMessage.INVALID_INPUT, vm.uiState.value.message)
        assertTrue(vm.uiState.value.exercises.first().sets.first().showErrors)
        assertFalse(vm.finishWorkout("push-day"))
        assertEquals(WorkoutMessage.EMPTY_WORKOUT, vm.uiState.value.message)
        assertTrue(repository.data.value.sessions.isEmpty())
        assertTrue(repository.data.value.activeSession!!.completedSets.isEmpty())
        vm.updateWeight("db-bench", 1, "0")
        vm.updateReps("db-bench", 1, "1")
        vm.setCompleted("db-bench", 1, true)
        assertEquals(0.0, repository.data.value.activeSession!!.completedSets.single().weightKg, 0.0)
    }

    @Test fun duplicatePlanEntriesCanBothBeRecorded() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        val plan = repository.createPlan("Repeated exercise", 1)
        repository.addExerciseToPlan(plan.id, "db-bench", 1, 12, 10.0)
        repository.addExerciseToPlan(plan.id, "db-bench", 1, 8, 20.0)
        repository.startWorkout(plan.id)
        val vm = model(repository)
        runCurrent()
        vm.setCompleted("db-bench", 1, true)
        vm.setCompleted("db-bench", 2, true)
        assertEquals(listOf(10.0, 20.0), repository.data.value.activeSession!!.completedSets.map { it.weightKg })
    }

    @Test fun previousResultsMatchSetNumberIncludingAnAddedSet() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        repository.startWorkout("push-day")
        repository.completeSet("db-bench", 1, 17.5, 8)
        repository.completeSet("db-bench", 4, 10.0, 15)
        repository.finishWorkout()
        repository.startWorkout("push-day")
        val vm = model(repository)
        runCurrent()
        val rows = vm.uiState.value.exercises.first().sets
        assertEquals(17.5, rows[0].previous!!.weightKg, 0.0)
        assertNull(rows[1].previous)
        vm.addSet("db-bench")
        assertEquals(15, vm.uiState.value.exercises.first().sets.last().previous?.reps)
    }

    @Test fun restoredRouteRequiresExplicitStartAndRejectsAnotherPlansActions() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        val vm = model(repository)
        runCurrent()
        assertFalse(vm.finishWorkout("push-day"))
        assertNull(repository.data.value.activeSession)
        assertTrue(vm.startWorkout("push-day"))
        runCurrent()
        val sessionId = repository.data.value.activeSession!!.id
        assertTrue(vm.startWorkout("push-day"))
        assertFalse(vm.startWorkout("pull-day"))
        assertFalse(vm.finishWorkout("pull-day"))
        assertFalse(vm.discardWorkout("pull-day"))
        assertEquals(sessionId, repository.data.value.activeSession!!.id)
        assertTrue(repository.data.value.sessions.isEmpty())
    }

    @Test fun unrelatedPlanChangesPreserveDraftsAndExtraRows() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        repository.startWorkout("push-day")
        val vm = model(repository)
        runCurrent()
        vm.updateWeight("db-bench", 1, "")
        vm.addSet("db-bench")
        repository.createPlan("Another plan", 2)
        runCurrent()
        assertEquals("", vm.uiState.value.exercises.first().sets.first().weight)
        assertEquals(4, vm.uiState.value.exercises.first().sets.size)
    }

    @Test fun discardingStopsTimerAndDoesNotLeakDraftsIntoTheNextWorkout() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        repository.startWorkout("push-day")
        val vm = model(repository)
        runCurrent()
        vm.updateWeight("db-bench", 1, "999")
        vm.setCompleted("db-bench", 1, true)
        assertTrue(vm.discardWorkout("push-day"))
        runCurrent()
        assertTrue(repository.data.value.sessions.isEmpty())
        assertFalse(vm.restTimer.value.running)
        assertTrue(vm.startWorkout("push-day"))
        runCurrent()
        assertEquals("15", vm.uiState.value.exercises.first().sets.first().weight)
    }

    @Test fun emptyPlansCannotCreateAnUnfinishableActiveWorkout() = runTest(dispatcher) {
        val repository = WorkoutRepository()
        val plan = repository.createPlan("Empty", 1)
        assertNull(repository.startWorkout(plan.id))
        val vm = model(repository)
        runCurrent()
        assertFalse(vm.startWorkout(plan.id))
        assertEquals(WorkoutMessage.EMPTY_PLAN, vm.uiState.value.message)
        assertNull(repository.data.value.activeSession)
    }
}
