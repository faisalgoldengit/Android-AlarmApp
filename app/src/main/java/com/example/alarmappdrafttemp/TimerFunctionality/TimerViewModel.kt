package com.example.alarmappdrafttemp.TimerFunctionality

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(private val context: Context) : ViewModel() {

    // UI States
    private val _timerState = MutableStateFlow(0L)
    val timerState = _timerState.asStateFlow()

    private val _timerTotal = MutableStateFlow(0L)
    val timerTotal = _timerTotal.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    init {
        // Observer: Listen to the Service via EventBus
        viewModelScope.launch {
            TimerEventBus.events.collect { state ->
                _timerState.value = state.timeLeft
                _timerTotal.value = state.totalTime
                _isRunning.value = state.isRunning
            }
        }
    }

    // --- Commands (Send to Service) ---

    fun startTimer(duration: Long) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START
            putExtra(TimerForegroundService.EXTRA_TIME, duration)
        }
        context.startService(intent)
    }

    fun pauseTimer() {
        sendCommand(TimerForegroundService.ACTION_PAUSE)
    }

    fun resumeTimer() {
        sendCommand(TimerForegroundService.ACTION_RESUME)
    }

    fun resetTimer() {
        sendCommand(TimerForegroundService.ACTION_CANCEL)
        // Optimistic UI update for instant feel
        _timerState.value = 0L
        _isRunning.value = false
    }

    private fun sendCommand(action: String) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}