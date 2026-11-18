package com.example.fittrack.ui.workout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RestTimerState(
    val durationSeconds: Int = 90,
    val remainingSeconds: Int = 90,
    val running: Boolean = false,
    val finished: Boolean = false
)

internal fun remainingRestSeconds(deadlineMs: Long, nowMs: Long): Int =
    ((deadlineMs - nowMs).coerceAtLeast(0) + 999).div(1000).toInt()

class RestTimer(private val scope: CoroutineScope, private val nowMs: () -> Long) {
    private val _state = MutableStateFlow(RestTimerState())
    val state = _state.asStateFlow()
    private var job: Job? = null
    private var deadlineMs = 0L

    fun setDuration(seconds: Int): Boolean {
        if (seconds !in 1..3600) return false
        job?.cancel()
        _state.value = RestTimerState(seconds, seconds)
        return true
    }

    fun start() {
        if (_state.value.running) return
        val seconds = _state.value.remainingSeconds.takeIf { it > 0 } ?: _state.value.durationSeconds
        deadlineMs = nowMs() + seconds * 1000L
        _state.value = _state.value.copy(remainingSeconds = seconds, running = true, finished = false)
        job = scope.launch {
            while (isActive) {
                val remaining = remainingRestSeconds(deadlineMs, nowMs())
                _state.value = _state.value.copy(remainingSeconds = remaining, running = remaining > 0, finished = remaining == 0)
                if (remaining == 0) break
                delay(200)
            }
        }
    }

    fun pause() {
        if (!_state.value.running) return
        job?.cancel()
        val remaining = remainingRestSeconds(deadlineMs, nowMs())
        _state.value = _state.value.copy(remainingSeconds = remaining, running = false, finished = remaining == 0)
    }

    fun reset() {
        job?.cancel()
        _state.value = RestTimerState(_state.value.durationSeconds, _state.value.durationSeconds)
    }

    fun restart() {
        reset()
        start()
    }

    fun skip() {
        job?.cancel()
        _state.value = _state.value.copy(remainingSeconds = 0, running = false, finished = false)
    }
}
