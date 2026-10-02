package com.example.alarmappdrafttemp.RoomSetup

import androidx.room.Room
import com.example.alarmappdrafttemp.AlarmFunctionality.AlarmViewModel
import com.example.alarmappdrafttemp.AlarmFunctionality.ScheduleAlarm
import com.example.alarmappdrafttemp.DB.AlarmDatabase
import com.example.alarmappdrafttemp.StopWatchFunctionality.StopwatchViewModel
import com.example.alarmappdrafttemp.TimerFunctionality.TimerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module // NEW


val appmodule = module {

    single<AlarmDatabase>{
        Room.databaseBuilder(
            androidContext(),
            AlarmDatabase::class.java,
            "App_Database"
        ).build()
    }

    single{
        get<AlarmDatabase>().alarmDao()
    }

    factory{
        ScheduleAlarm(context = androidContext())
    }

    viewModel{
        AlarmViewModel(scheduler = get(), alarmDao = get())
    }
    viewModel{
        TimerViewModel(context= androidContext())

    }

    viewModel{
        StopwatchViewModel(get())

    }

}