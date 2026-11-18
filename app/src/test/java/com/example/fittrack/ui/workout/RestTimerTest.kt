package com.example.fittrack.ui.workout

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestTimerTest {
    @Test fun delayedTickUsesElapsedTimeInsteadOfCountingTicks() = runTest {
        var clock = 0L
        val timer = RestTimer(backgroundScope) { clock }
        timer.start()
        runCurrent()
        clock = 65_123
        advanceTimeBy(200)
        runCurrent()
        assertEquals(25, timer.state.value.remainingSeconds)
        assertTrue(timer.state.value.running)
    }

    @Test fun pauseAndResumePreserveRemainingRest() = runTest {
        val timer = RestTimer(backgroundScope) { testScheduler.currentTime }
        timer.setDuration(60)
        timer.start()
        advanceTimeBy(10_000)
        timer.pause()
        assertEquals(50, timer.state.value.remainingSeconds)
        advanceTimeBy(30_000)
        assertEquals(50, timer.state.value.remainingSeconds)
        timer.start()
        advanceTimeBy(50_000)
        runCurrent()
        assertEquals(0, timer.state.value.remainingSeconds)
        assertTrue(timer.state.value.finished)
        assertFalse(timer.state.value.running)
    }

    @Test fun restartingAfterAnotherSetCancelsTheOldDeadline() = runTest {
        val timer = RestTimer(backgroundScope) { testScheduler.currentTime }
        timer.setDuration(60)
        timer.start()
        advanceTimeBy(20_000)
        timer.restart()
        advanceTimeBy(40_000)
        runCurrent()
        assertEquals(20, timer.state.value.remainingSeconds)
        assertTrue(timer.state.value.running)
        timer.skip()
        assertFalse(timer.state.value.running)
        assertFalse(timer.state.value.finished)
        timer.reset()
        assertEquals(60, timer.state.value.remainingSeconds)
    }

    @Test fun customDurationRejectsInvalidValuesWithoutChangingSelection() = runTest {
        val timer = RestTimer(backgroundScope) { testScheduler.currentTime }
        assertTrue(timer.setDuration(120))
        assertFalse(timer.setDuration(0))
        assertFalse(timer.setDuration(3601))
        assertEquals(120, timer.state.value.durationSeconds)
        assertEquals(1, remainingRestSeconds(1000, 999))
        assertEquals(0, remainingRestSeconds(1000, 2000))
    }
}
