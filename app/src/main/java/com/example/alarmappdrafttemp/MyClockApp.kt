package com.example.alarmappdrafttemp

import android.app.Application
import com.example.alarmappdrafttemp.RoomSetup.appmodule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyClockApp)
            modules(appmodule)
        }
    }
}
