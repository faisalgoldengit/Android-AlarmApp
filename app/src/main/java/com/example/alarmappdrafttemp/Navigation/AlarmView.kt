package com.example.alarmappdrafttemp.Navigation

import AlarmDetailScreen
import AlarmScreen
import Grid3x3Loader
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.example.alarmappdrafttemp.AlarmFunctionality.AlarmViewModel
import com.example.alarmappdrafttemp.StopWatchFunctionality.GlassStopwatchScreen
import com.example.alarmappdrafttemp.TimerFunctionality.TimerScreen
import com.example.alarmappdrafttemp.TimerFunctionality.TimerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreenNav() {
    val color = Color(0xFF5526FF)
    val backStack = rememberNavBackStack(Screen.AlarmHome)
    val viewModel: AlarmViewModel = koinViewModel()
    val timerViewModel: TimerViewModel = koinViewModel()
    val loadingbar by viewModel.loadingbar.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                when (backStack.last()) {
                    Screen.Timer -> {
                        // Since the TimerService is now the source of truth,
                        // we don't need to manually start/stop it here.
                        // We simply minimize the app if that is the desired Back behavior.
                        val activity = context as? Activity
                        activity?.moveTaskToBack(true)
                    }
                    Screen.AlarmDetail -> {
                        backStack.removeLastOrNull()
                    }
                    else -> backStack.removeLastOrNull()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Screen.AlarmHome> {
                    AlarmScreen(
                        onsaveClick = { backStack.add(Screen.AlarmDetail) },
                        viewModel,
                        onTimerClick = { backStack.add(Screen.Timer) },
                        onAlarmClick = {},
                        onStopwatchClick = { backStack.add(Screen.StopWatch) },
                        onSettingsClick = { backStack.add(Screen.Timer) }
                    )
                }
                entry<Screen.AlarmDetail> {
                    AlarmDetailScreen({ backStack.removeLastOrNull() }, viewModel)
                }
                entry<Screen.Timer> {
                    TimerScreen(
                        onAlarmClick = { backStack.add(Screen.AlarmHome) },
                        onStopwatchClick = { backStack.add(Screen.StopWatch) },
                        onSettingsClick = {},
                        timerViewModel
                    )
                }

                entry<Screen.StopWatch>{
                    GlassStopwatchScreen(
                        onAlarmClick = { backStack.add(Screen.AlarmHome) },
                        onTimerClick = { backStack.add(Screen.Timer)}
                    )
                }
            }
        )

        if (loadingbar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Grid3x3Loader(size = 50.dp, color = color)
            }
        }
    }
}