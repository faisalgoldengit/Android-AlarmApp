package com.example.alarmappdrafttemp.TimerFunctionality

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TimerEventBus {
    data class TimerState(
        val timeLeft: Long,        // Current remaining milliseconds
        val totalTime: Long,       // The original set time (for progress calculation)
        val isRunning: Boolean,    // Is the timer currently ticking?
        val isCancelled: Boolean   // Has the timer been stopped/reset?
    )

    private val _events = MutableSharedFlow<TimerState>(
        replay = 1,
        extraBufferCapacity = 1
    )

    val events = _events.asSharedFlow()

    fun emit(state: TimerState) {
        _events.tryEmit(state)
    }
}