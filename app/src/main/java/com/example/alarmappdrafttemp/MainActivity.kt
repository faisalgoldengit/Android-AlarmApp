package com.example.alarmappdrafttemp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.alarmappdrafttemp.Navigation.AlarmScreenNav
import com.example.alarmappdrafttemp.TimerFunctionality.TimerViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AlarmScreenNav()
        }
    }


}
