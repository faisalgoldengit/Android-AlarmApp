package com.example.alarmappdrafttemp.DB

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.alarmappdrafttemp.AlarmFunctionality.LocalDateTimeAdapter
import com.google.gson.GsonBuilder
import java.time.LocalDateTime

@Entity(tableName="Alarm_Table")
data class Alarm_Items_db(
    val alarmitem: AlarmItem,
    @PrimaryKey(autoGenerate = true) val id:Int =0


)


class AlarmrepoConverter{
//    private val gson = Gson()
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    @TypeConverter
    fun fromAlarmItem(alarmItem: AlarmItem):String{
//        return "${alarmItem.time},${alarmItem.message},${alarmItem.turnedOn},${for(day in alarmItem.days)}"
        return gson.toJson(alarmItem)
    }

    @TypeConverter
    fun toAlarmItem(data:String): AlarmItem {
//        val parts = data.split(",")
//        return AlarmItem(LocalDateTime.parse(parts[0]),parts[1],parts[2].toBoolean())
        return gson.fromJson(data, AlarmItem::class.java)
    }

}