package com.example.alarmappdrafttemp.DB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmItem(alarmItem: Alarm_Items_db)

    @Delete
    suspend fun deleteAlarmItem(alarmItem: Alarm_Items_db)

    @Query("SELECT * FROM Alarm_Table")
    suspend fun getAlarmItems(): List<Alarm_Items_db>
}