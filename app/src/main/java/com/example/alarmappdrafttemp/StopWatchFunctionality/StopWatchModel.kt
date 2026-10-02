package com.example.alarmappdrafttemp.StopWatchFunctionality


data class StopWatchData(
    val TimeElapsed:Long = 0L,//TotalTimeTIll NOw
    val isRunning: Boolean=false,
    val Laps:MutableList<LapData> = emptyList<LapData>().toMutableList()

)

data class LapData(
    val LapNumber:Int,
    val LapTime:Long,//Total TIme since Start
    val splitTime:Long // Time since Last Lap
)
