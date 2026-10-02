package com.example.alarmappdrafttemp.AlarmFunctionality


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.os.Build
import com.example.alarmappdrafttemp.DB.AlarmItem

import com.google.gson.GsonBuilder
import java.time.LocalDateTime

class AlarmReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent==null){return}
        if (context==null){return}

//            val gson = Gson()

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()

        val serviceIntent = Intent(context, AlarmService1::class.java)
            val repeat = intent.getStringExtra("REPEAT")
            val alarmitem = gson.fromJson(repeat, AlarmItem::class.java)
            val scheduler = ScheduleAlarm(context.applicationContext)
//            val localDateTime = Instant
//            .ofEpochMilli(alarmitem.timeInMillis)
//            .atZone(ZoneId.systemDefault())
//            .toLocalDateTime()
            scheduler.schedule(alarmitem.copy(time=alarmitem.time.plusDays(1)))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // This is allowed because we were triggered by an Exact Alarm
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

    }
}