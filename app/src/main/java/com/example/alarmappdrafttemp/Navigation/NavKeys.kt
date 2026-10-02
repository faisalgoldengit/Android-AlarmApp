package com.example.alarmappdrafttemp.Navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable


sealed interface Screen: NavKey{

    @Serializable
    data object AlarmHome: Screen

    @Serializable
    data object AlarmDetail: Screen

    @Serializable
    data object Timer:Screen

    @Serializable
    data object StopWatch:Screen

}