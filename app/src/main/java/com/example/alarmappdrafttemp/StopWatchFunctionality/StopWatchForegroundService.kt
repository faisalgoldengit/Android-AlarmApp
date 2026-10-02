package com.example.alarmappdrafttemp.StopWatchFunctionality

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.alarmappdrafttemp.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StopWatchForegroundService : Service(), LifecycleEventObserver {

    private var startTime = 0L
    private var lapnum = 0
    private var TotalTimeElapsed = 0L
    private var LastLapTotalTime = 0L
    private var accumulatedTime = 0L
    private var isRunning = false
    private var isAppinForeground = false

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StopWatchForegroundService = this@StopWatchForegroundService
    }

    private var _StopWatchState = MutableStateFlow(StopWatchData())
    val stopWatchState = _StopWatchState.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWatch()
            ACTION_PAUSE -> pauseWatch()
            ACTION_RESUME -> resumeWatch()
            ACTION_STOP -> stopWatch()
        }

        createNotificationChannel()
        if (intent?.action == null) {
            updateNotification(true)
        }

        return START_STICKY
    }

    fun startWatch() {
        if (isRunning) return
        isRunning = true
        startTime = System.currentTimeMillis()

        // Start foreground immediately to update buttons
        startForeground(NOTIFICATION_ID, buildNotification(isAppinForeground))

        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isRunning) {
                val CurrentTime = System.currentTimeMillis()
                TotalTimeElapsed = CurrentTime - startTime + accumulatedTime

                _StopWatchState.value = _StopWatchState.value.copy(TimeElapsed = TotalTimeElapsed, isRunning = true)

                if (!isAppinForeground && TotalTimeElapsed % 1000 < 50) {
                    updateNotification(false)
                }
                delay(50)
            }
        }
    }

    fun pauseWatch() {
        if (!isRunning) return
        timerJob?.cancel()
        accumulatedTime = TotalTimeElapsed
        isRunning = false
        _StopWatchState.value = _StopWatchState.value.copy(isRunning = false, TimeElapsed = accumulatedTime)
        updateNotification(isAppinForeground)
    }

    fun resumeWatch() {
        startWatch()
    }

    fun stopWatch() {
        isRunning = false
        timerJob?.cancel()
        accumulatedTime = 0L
        TotalTimeElapsed = 0L
        LastLapTotalTime = 0L
        lapnum = 0
        startTime = 0L

        _StopWatchState.value = StopWatchData() // Reset State

        // Remove notification and kill service
        stopForeground(true)
        stopSelf()
    }

    fun recordLap() {
        lapnum += 1
        val splitTime = TotalTimeElapsed - LastLapTotalTime
        LastLapTotalTime = TotalTimeElapsed

        val newLap = LapData(lapnum, TotalTimeElapsed, splitTime)
        val currentList = _StopWatchState.value.Laps.toMutableList()
        currentList.add(0, newLap)

        _StopWatchState.value = _StopWatchState.value.copy(Laps = currentList)
    }

    private fun updateNotification(foreStaticMessage: Boolean = false) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(foreStaticMessage))
    }

    private fun buildNotification(foreStaticMessage: Boolean = false): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- Create Actions ---
        val pausePendingIntent = getServicePendingIntent(ACTION_PAUSE)
        val resumePendingIntent = getServicePendingIntent(ACTION_RESUME)
        val stopPendingIntent = getServicePendingIntent(ACTION_STOP) // <--- STOP Intent

        val builder_notify = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setContentText("Stopwatch Running")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setTimeoutAfter(2000L)
            .setAutoCancel(true)

        val builder_live = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setContentTitle("Stopwatch")
            .setContentText(formatTime(TotalTimeElapsed))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)

        // --- Add Buttons ---
        if (isRunning) {
            // 1. Pause
            builder_live.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
        } else {
            // 1. Resume
            builder_live.addAction(android.R.drawable.ic_media_play, "Resume", resumePendingIntent)
        }

        // 2. Stop (Cancel) Button - Always visible in the expanded notification
        builder_live.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)

        if (isAppinForeground) {
            return builder_notify.build()
        } else {
            return builder_live.build()
        }
    }

    private fun getServicePendingIntent(actionStr: String): PendingIntent {
        val intent = Intent(this, StopWatchForegroundService::class.java).apply {
            action = actionStr
        }
        return PendingIntent.getService(
            this,
            actionStr.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "StopWatchService", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val CHANNEL_ID = "LiveServiceChannel"
        const val NOTIFICATION_ID = 5
    }

    override fun onCreate() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        super.onCreate()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                isAppinForeground = true
                updateNotification(foreStaticMessage = true)
            }
            Lifecycle.Event.ON_STOP -> {
                isAppinForeground = false
                updateNotification(isAppinForeground)
            }
            else -> {}
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        val hundredths = (millis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
    }
}