package com.example.alarmappdrafttemp.StopWatchFunctionality

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    // 1. WeakReference prevents Memory Leaks (Context Leakage)
    private var stopwatchServiceRef: WeakReference<StopWatchForegroundService>? = null
    private var isBound = false

    // 2. State for the UI to observe (Source of Truth comes from Service)
    private val _state = MutableStateFlow(StopWatchData())
    val state = _state.asStateFlow()

    // 3. The Connection Listener
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as StopWatchForegroundService.LocalBinder
            val serviceInstance = binder.getService()

            // Store service safely
            stopwatchServiceRef = WeakReference(serviceInstance)
            isBound = true

            // Connect Service's StateFlow -> ViewModel's StateFlow
            // We collect in a coroutine so UI updates automatically
            viewModelScope.launch {
                serviceInstance.stopWatchState.collect {
                    _state.value = it
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            stopwatchServiceRef = null
        }
    }

    init {
        // 4. Bind to service immediately when ViewModel is created
        val intent = Intent(application, StopWatchForegroundService::class.java)
        println("Started")

        // Start service first (so it keeps running even if we unbind/rotate screen)
        application.startService(intent)

        // Bind to it to enable function calls (Start/Stop/Lap)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // --- UI ACTIONS (Called from Composable) ---
    // We safely unwrap the weak reference using ?.get()

    fun toggleStartPause() {
        val service = stopwatchServiceRef?.get()
        if (service != null) {
            if (_state.value.isRunning) {
                service.pauseWatch()
            } else {
                service.startWatch()
            }
        }
    }

    fun reset() {
        stopwatchServiceRef?.get()?.stopWatch()
    }

    fun lap() {
        stopwatchServiceRef?.get()?.recordLap()
    }

    // 5. Cleanup when ViewModel is destroyed (e.g., closing the app)
    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(connection)
            isBound = false
        }
    }
}