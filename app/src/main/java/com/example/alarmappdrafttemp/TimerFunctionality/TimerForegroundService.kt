package com.example.alarmappdrafttemp.TimerFunctionality

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.alarmappdrafttemp.MainActivity
import com.example.alarmappdrafttemp.R
import kotlinx.coroutines.*

class TimerForegroundService : Service(), LifecycleEventObserver {

    // Core State
    private var timeLeft = 0L
    private var totalTime = 0L
    private var isRunning = false
    private var SetTime = 0L

    private var isAppinForeground = true

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        when (intent?.action) {
            ACTION_START -> {
                val inputTime = intent.getLongExtra(EXTRA_TIME, 0L)
                if (inputTime > 0) {
                    startTimer(inputTime)
                }
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_CANCEL -> stopTimer() // Changed from RESET to STOP logic
            ACTION_STOP -> stopTimer()   // Stop action (e.g. from Alarm notification)
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopTimer()
        super.onTaskRemoved(rootIntent)
    }

    private fun startTimer(duration: Long) {
        timeLeft = duration
        totalTime = duration
        SetTime = duration
        isRunning = true

        // Start Foreground immediately
        startForeground(NOTIFICATION_ID, buildNotification(isAppinForeground))
        startCountdown()
    }

    private fun resumeTimer() {
        if (timeLeft > 0) {
            isRunning = true
            startCountdown()
        }
    }

    private fun pauseTimer() {
        isRunning = false
        timerJob?.cancel()
        updateNotification(isAppinForeground)
        publishState()
    }

    // Renamed logic to stop service completely
    private fun stopTimer() {
        isRunning = false
        timerJob?.cancel()
        timeLeft = 0
        totalTime = 0

        publishState() // Clear UI
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (timeLeft > 0 && isRunning) {
                publishState()
                updateNotification(isAppinForeground)

                delay(1000L)
                timeLeft -= 1000L
            }

            if (timeLeft <= 0) {
                timeLeft = 0
                isRunning = false
                publishState()

                // --- TIMER FINISHED LOGIC ---
                triggerAlarmNotification()
            }
        }
    }

    private fun publishState() {
        TimerEventBus.emit(
            TimerEventBus.TimerState(
                timeLeft = timeLeft,
                totalTime = totalTime,
                isRunning = isRunning,
                isCancelled = (timeLeft == 0L && !isRunning)
            )
        )
    }

    // --- Notification Helpers ---

    private fun updateNotification(foreStaticMessage: Boolean = false) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // If timer is finished (0), don't update the progress bar notification,
        // because the Alarm notification is taking over.
        if (timeLeft > 0) {
            manager.notify(NOTIFICATION_ID, buildNotification(foreStaticMessage))
        }
    }

    private fun triggerAlarmNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use a default alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Stop intent for the alarm notification
        val stopPendingIntent = getPendingIntent(ACTION_STOP)

        val alarmNotification = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Timer Finished!")
            .setContentText("Your timer has ended.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High Priority for Heads-up
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibrate pattern
            .setAutoCancel(true)
            .setFullScreenIntent(getPendingIntent(ACTION_STOP), true) // Optional: wake screen
            .build()

        // We use a different ID so it replaces or sits on top of the running one
        manager.notify(ALARM_NOTIFICATION_ID, alarmNotification)
    }

    private fun buildNotification(foreStaticMessage: Boolean = false): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate progress
        val progress = if (totalTime > 0) {
            (((totalTime - timeLeft).toFloat() / totalTime) * 100).toInt()
        } else 0

        val (h, m, s) = millisToTimeParts(timeLeft)
        val formattedTime = String.format("%02d:%02d:%02d", h, m, s)

        val remoteViews = RemoteViews(packageName, R.layout.notification_thick_bar)
        remoteViews.setTextViewText(R.id.tv_timer, "$formattedTime remaining")
        remoteViews.setProgressBar(R.id.progress_bar, 100, progress, false)

        // 1. Static Notification (App in Foreground)
        val builder_notify = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setContentText("Timer Opened")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setTimeoutAfter(2000L)
            .setAutoCancel(true)

        // 2. Live Notification (App in Background)
        val builder_live = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOnlyAlertOnce(true)
            .setOngoing(true) // Persistent while running
            .setSilent(true)

        // Add Actions based on state
        if (isRunning) {
            builder_live.addAction(android.R.drawable.ic_media_pause, "Pause", getPendingIntent(ACTION_PAUSE))
        } else {
            builder_live.addAction(android.R.drawable.ic_media_play, "Resume", getPendingIntent(ACTION_RESUME))
        }

        // CHANGED: "Reset" -> "Cancel"
        builder_live.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", getPendingIntent(ACTION_CANCEL))

        if(foreStaticMessage){
            return builder_notify.build()
        } else {
            return builder_live.build()
        }
    }

    private fun getPendingIntent(actionStr: String): PendingIntent {
        val intent = Intent(this, TimerForegroundService::class.java).apply { action = actionStr }
        return PendingIntent.getService(this, actionStr.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // 1. Silent Channel for the Progress Bar
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Timer Silent", NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(serviceChannel)

            // 2. High Priority Channel for the Alarm Sound
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID, "Timer Alarm", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Timer Finished Alarms"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            manager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "LiveServiceChannel"
        const val ALARM_CHANNEL_ID = "TimerAlarmChannel" // New Channel ID

        const val NOTIFICATION_ID = 1
        const val ALARM_NOTIFICATION_ID = 2 // New Notification ID

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_CANCEL = "ACTION_CANCEL" // Renamed from RESET
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_TIME = "EXTRA_TIME"
    }

    override fun onCreate() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        super.onCreate()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_RESUME ->{
                isAppinForeground=true
                updateNotification(foreStaticMessage = true)
            }
            Lifecycle.Event.ON_STOP->{
                isAppinForeground=false
                updateNotification(isAppinForeground)
            }
            else -> {}
        }
    }
}