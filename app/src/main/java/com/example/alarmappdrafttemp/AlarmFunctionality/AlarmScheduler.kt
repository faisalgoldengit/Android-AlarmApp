package com.example.alarmappdrafttemp.AlarmFunctionality

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarmappdrafttemp.DB.AlarmItem
import com.google.gson.GsonBuilder
import java.time.LocalDateTime
import java.time.ZoneId

interface AlarmScheduler{
    fun schedule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}

fun pendingIntentFlag():Int{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
}
class ScheduleAlarm(
    private val context: Context
): AlarmScheduler{

    private val alarmManager = context.getSystemService(AlarmManager::class.java)?: throw IllegalStateException("AlarmManager not available")
    @SuppressLint("ScheduleExactAlarm")
    override fun schedule(item: AlarmItem) {
        val newTime = nextDay(item,item.time)
//        val newTime = item.time
        if (newTime>item.time){return}
//        val gson = Gson()
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()

        val json = gson.toJson(item)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REPEAT", json)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.hashCode(),
            intent,
            pendingIntentFlag()
        )


        val triggerTime = newTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, // Wakes up the device if screen is off
            triggerTime,
            pendingIntent
        )


    }
    override fun cancel(item: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun nextDay(item: AlarmItem, newtime: LocalDateTime): LocalDateTime{
        val today =newtime.dayOfWeek.value
        var newTime = newtime
        var timeselect = true
        if(item.days.indexOf(today)==-1){
            for(days in item.days){
                if(days>today){
                    val inc = days-today
                    newTime = newTime.plusDays(inc.toLong())}
                timeselect=false
            }
            if(timeselect){
                newTime = newTime.plusDays(item.days.first().toLong()-today.toLong())
            }
        }
        return newTime

    }

}