package com.example.alarmappdrafttemp.DB

import java.time.LocalDateTime

data class AlarmItem(
    val time: LocalDateTime,
    val message:String,
    val turnedOn: Boolean = true,
    val days: Set<Int>

)