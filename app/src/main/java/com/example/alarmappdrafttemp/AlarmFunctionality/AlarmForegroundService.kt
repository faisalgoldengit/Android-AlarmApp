package com.example.alarmappdrafttemp.AlarmFunctionality

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.IBinder
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.DisposableEffect
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlin.jvm.java


class AlarmService1 : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive

        when (intent?.action) {
            "ACTION_STOP_ALARM" -> {
                stopAlarm()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            "ACTION_SNOOZE" -> {
                stopAlarm()
                // schedule next alarm here
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // 1. Create the Full-Screen Intent (The Activity showing Snooze/Dismiss)
        val channelId = "ALARM_CHANNEL"
        val fullScreenIntent = Intent(this, AlarmActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH

            ).apply {
                description = "Alarm alerts"
                setSound(null,null)
//                setSound(
//                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
//                    AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ALARM)
//                        .build()
//                )
//                enableVibration(true)
//                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }

            val manager =
                getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val dismissIntent = Intent(this, AlarmService1::class.java).apply {
            action = "ACTION_STOP_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE)




        val notificationBuilder_Compressed = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText("Tap to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)

        // 2. Build Notification
        // HIGH IMPORTANCE is crucial for Alarms
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText("Tap to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // CRITICAL: Wakes screen
            .setOngoing(true) // User cannot swipe it away easily

        // 3. Start Foreground
//        startForeground(1, notificationBuilder.build())

        if(isScreenOn){
            ServiceCompat.startForeground(this, 1, notificationBuilder_Compressed.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)

        }
        else{
            ServiceCompat.startForeground(this, 1, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)

        }

        // 4. Play Sound & Vibrate
        startAlarmSound()

        return START_STICKY
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        stopSelf()
    }


    private fun startAlarmSound() {
        // Initialize MediaPlayer logic here to play ringtone
        // Initialize Vibrator logic here
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@AlarmService1, uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
            prepare()
            start()
        }

    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}