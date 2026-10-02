//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.alarmappdrafttemp.DB.AlarmItem
//import com.example.alarmappdrafttemp.AlarmFunctionality.AlarmViewModel
//import org.koin.androidx.compose.koinViewModel
//import java.time.LocalDate
//import java.util.Calendar
//
//// --- Colors ---
//val GoogleBlack_detail = Color(0xFF000000)
//val GoogleDarkGrey_detail = Color(0xFF202124)
//val GoogleBlue_detail = Color(0xFF8AB4F8)
//val TextGrey_detail = Color(0xFF9AA0A6)
//val DaySelectedColor = GoogleBlue_detail
//val DayUnselectedColor = Color.Transparent
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AlarmDetailScreen(saveBack: ()->Unit = {},viewModel: AlarmViewModel) {
////    val viewModel: AlarmViewModel = koinViewModel()
//    // State for Time Picker
//    var showTimePicker by remember { mutableStateOf(false) }
//    val timeState = rememberTimePickerState(
//        initialHour = 6,
//        initialMinute = 0,
//        is24Hour = false
//    )
//
//    // Form State
//    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // Mon-Fri default
//    var isVibrateEnabled by remember { mutableStateOf(true) }
//    var ringtoneName by remember { mutableStateOf("Default (Oxygen)") }
//    var labelText by remember { mutableStateOf("") }
//
//    // Helper to format time for display
//    val formattedTime = remember(timeState.hour, timeState.minute) {
//        val cal = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, timeState.hour)
//            set(Calendar.MINUTE, timeState.minute)
//        }
//        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
//        val hour = if (cal.get(Calendar.HOUR) == 0) 12 else cal.get(Calendar.HOUR)
//        val minute = String.format("%02d", cal.get(Calendar.MINUTE))
//        "$hour:$minute $amPm"
//    }
//    Scaffold(
//        containerColor = GoogleBlack_detail,
//        topBar = {
//            TopAppBar(
//                title = {},
//                navigationIcon = {
//                    TextButton(onClick = { /* Handle Cancel */ }) {
//                        Text("Cancel", color = TextGrey_detail)
//                    }
//                },
//                actions = {
//                    Button(
//                        onClick = {
//                            val localDateTime = LocalDate.now().atTime(timeState.hour, timeState.minute)
//                            viewModel.scheduleAlarm(AlarmItem(localDateTime, "test",true),0)
////                            viewModel.GetAlarms()
//                            saveBack()
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue_detail),
//                        contentPadding = PaddingValues(horizontal = 24.dp)
//                    ) {
//                        Text("Save", color = GoogleDarkGrey_detail)
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = GoogleBlack_detail)
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .padding(horizontal = 16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            // 1. Large Clickable Time Display
//            Box(
//                modifier = Modifier
//                    .padding(vertical = 32.dp)
//                    .clickable { showTimePicker = true }
//            ) {
//                Text(
//                    text = formattedTime,
//                    fontSize = 80.sp,
//                    fontWeight = FontWeight.Light,
//                    color = Color.White
//                )
//            }
//
//            // 2. Repeat / Days Section
//            Text(
//                text = "Repeat",
//                color = Color.White,
//                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                // S M T W T F S
//                val days = listOf("S", "M", "T", "W", "T", "F", "S")
//                days.forEachIndexed { index, dayName ->
//                    DayToggleCircle(
//                        text = dayName,
//                        isSelected = selectedDays.contains(index),
//                        onClick = {
//                            selectedDays = if (selectedDays.contains(index)) {
//                                selectedDays - index
//                            } else {
//                                selectedDays + index
//                            }
//                        }
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // 3. Options List (Ringtone, Vibrate, Label)
//            Column(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(GoogleDarkGrey_detail)
//            ) {
//                OptionItem(
//                    icon = Icons.Default.Notifications,
//                    title = "Alarm sound",
//                    subtitle = ringtoneName,
//                    onClick = { /* Open Ringtone Picker */ }
//                )
//
//                Divider(color = GoogleBlack_detail, thickness = 1.dp)
//
//                OptionItemSwitch(
//                    icon = Icons.Default.Vibration,
//                    title = "Vibrate",
//                    isChecked = isVibrateEnabled,
//                    onCheckedChange = { isVibrateEnabled = it }
//                )
//
//                Divider(color = GoogleBlack_detail, thickness = 1.dp)
//
//                OptionItem(
//                    icon = Icons.Default.Label,
//                    title = "Label",
//                    subtitle = if (labelText.isEmpty()) "Add label" else labelText,
//                    onClick = { /* Open Label Dialog */ }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // 4. Delete Button
//            TextButton(
//                onClick = { /* Handle Delete */ },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp)
//                    .clip(RoundedCornerShape(25.dp))
//                    .background(GoogleDarkGrey_detail),
//            ) {
//                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE2746B))
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Delete", color = Color(0xFFE2746B))
//            }
//        }
//    }
//
//    // Modal Time Picker Dialog
//    if (showTimePicker) {
//        TimePickerDialog(
//            onDismiss = { showTimePicker = false },
//            onConfirm = { showTimePicker = false }
//        ) {
//            TimePicker(state = timeState)
//        }
//    }
//}
//
//// --- Helper Composables ---
//
//@Composable
//fun DayToggleCircle(
//    text: String,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .size(40.dp)
//            .clip(CircleShape)
//            .background(if (isSelected) GoogleBlue_detail else Color.Transparent)
//            .border(
//                width = 1.dp,
//                color = if (isSelected) Color.Transparent else TextGrey_detail,
//                shape = CircleShape
//            )
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = if (isSelected) GoogleDarkGrey_detail else TextGrey_detail,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}
//
//@Composable
//fun OptionItem(
//    icon: ImageVector,
//    title: String,
//    subtitle: String,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(icon, contentDescription = null, tint = TextGrey_detail)
//        Spacer(modifier = Modifier.width(16.dp))
//        Column(modifier = Modifier.weight(1f)) {
//            Text(title, color = Color.White, fontSize = 16.sp)
//            Text(subtitle, color = GoogleBlue_detail, fontSize = 14.sp)
//        }
//    }
//}
//
//@Composable
//fun OptionItemSwitch(
//    icon: ImageVector,
//    title: String,
//    isChecked: Boolean,
//    onCheckedChange: (Boolean) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(icon, contentDescription = null, tint = TextGrey_detail)
//        Spacer(modifier = Modifier.width(16.dp))
//        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
//        Switch(
//            checked = isChecked,
//            onCheckedChange = onCheckedChange,
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = GoogleDarkGrey_detail,
//                checkedTrackColor = GoogleBlue_detail,
//                uncheckedThumbColor = TextGrey_detail,
//                uncheckedTrackColor = Color.Transparent,
//                uncheckedBorderColor = TextGrey_detail
//            )
//        )
//    }
//}
//
//// Boilerplate for TimePickerDialog (Material3 doesn't have a built-in Dialog wrapper yet)
//@Composable
//fun TimePickerDialog(
//    onDismiss: () -> Unit,
//    onConfirm: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        confirmButton = {
//            TextButton(onClick = onConfirm) { Text("OK") }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) { Text("Cancel") }
//        },
//        text = { content() }
//    )
//}
//
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmappdrafttemp.DB.AlarmItem
import com.example.alarmappdrafttemp.AlarmFunctionality.AlarmViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

// --- Aesthetic Colors ---
val DeepIndigo_detail = Color(0xFF1E1B4B)  // Darker Indigo base
val SlateDark_detail = Color(0xFF0F172A)
val NeonPurple_detail = Color(0xFF818CF8)  // Primary Accent
val NeonGlow = Color(0xFF818CF8)
val GlassBg = Color.White.copy(alpha = 0.05f)
val GlassBorder_detail = Color.White.copy(alpha = 0.1f)
val TextWhite_detail = Color(0xFFF1F5F9)
val TextDim_detail = Color(0xFF94A3B8)
val RedGlass = Color(0xFFEF4444).copy(alpha = 0.15f)
val RedText = Color(0xFFF87171)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(saveBack: () -> Unit = {}, viewModel: AlarmViewModel) {

    // --- State ---
    var showTimePicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        initialHour = 6,
        initialMinute = 0,
        is24Hour = false
    )

    var selectedDays by remember { mutableStateOf(setOf(LocalDateTime.now().dayOfWeek.value)) } // Mon-Fri
    var isVibrateEnabled by remember { mutableStateOf(true) }
    var ringtoneName by remember { mutableStateOf("Default (Oxygen)") }
    var labelText by remember { mutableStateOf("Morning Run") }

    // --- Helper for Time Display ---
    val (formattedTime, amPm) = remember(timeState.hour, timeState.minute) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeState.hour)
            set(Calendar.MINUTE, timeState.minute)
        }
        val amPmStr = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        val hour = if (cal.get(Calendar.HOUR) == 0) 12 else cal.get(Calendar.HOUR)
        val minute = String.format("%02d", cal.get(Calendar.MINUTE))
        Pair("$hour:$minute", amPmStr)
    }

    // --- Background Gradient ---
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DeepIndigo_detail, SlateDark_detail, Color.Black)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Decorative Glow Blobs (Matching the image vibe)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-50).dp)
                .size(300.dp)
                .background(Color(0xFF9333EA).copy(alpha = 0.2f), CircleShape)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Custom Transparent Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { saveBack() }) {
                        Text("Cancel", color = TextWhite_detail.copy(alpha = 0.7f), fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            // --- YOUR VIEWMODEL LOGIC PRESERVED ---
                            if(selectedDays.isEmpty()){selectedDays=selectedDays+ LocalDateTime.now().dayOfWeek.value}
                            val localDateTime = LocalDate.now().atTime(timeState.hour, timeState.minute)
                            viewModel.scheduleAlarm(AlarmItem(localDateTime, "test", true, days = selectedDays), 0)
                            saveBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple_detail),
                        shape = RoundedCornerShape(50), // Fully rounded
                        // Add Neon Shadow
                        modifier = Modifier.shadow(15.dp, RoundedCornerShape(50), spotColor = NeonGlow)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Time Display (Split for styling)
                Row(
                    modifier = Modifier
                        .clickable { showTimePicker = true }
                        .padding(vertical = 20.dp),
                    verticalAlignment = Alignment.Bottom // Align AM/PM to bottom
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Light,
                        color = TextWhite_detail,
                        letterSpacing = (-2).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = amPm,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDim_detail,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Repeat Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "REPEAT",
                        color = TextWhite_detail.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("M", "T", "W", "T", "F", "S","S")
                        days.forEachIndexed { index, dayName ->
                            val dayvlaue = index+1

                            AestheticDayToggle(
                                text = dayName,
                                isSelected = selectedDays.contains(dayvlaue),
                                onClick = {
                                    selectedDays = if (selectedDays.contains(dayvlaue)) {
                                        selectedDays - dayvlaue
                                    } else {
                                        selectedDays + dayvlaue
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Settings Panel (Glass)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassBg)
                        .border(1.dp, GlassBorder_detail, RoundedCornerShape(24.dp))
                ) {
                    // Alarm Sound
                    GlassOptionItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Alarm sound",
                        subtitle = ringtoneName,
                        onClick = { /* Ringtone Picker */ }
                    )

                    Divider(color = GlassBorder_detail, thickness = 1.dp)

                    // Vibrate
                    GlassSwitchItem(
                        icon = Icons.Outlined.Vibration,
                        title = "Vibrate",
                        isChecked = isVibrateEnabled,
                        onCheckedChange = { isVibrateEnabled = it }
                    )

                    Divider(color = GlassBorder_detail, thickness = 1.dp)

                    // Label
                    GlassOptionItem(
                        icon = Icons.Outlined.Label,
                        title = "Label",
                        subtitle = labelText,
                        onClick = { /* Label Input */ }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. Delete Button (Red Glass)
                Button(
                    onClick = { /* Handle Delete */ },
                    colors = ButtonDefaults.buttonColors(containerColor = RedGlass),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = RedText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Alarm", color = RedText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Modal Time Picker
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { showTimePicker = false }
        ) {
            TimePicker(state = timeState)
        }
    }
}

// --- Helper Composables ---

@Composable
fun AestheticDayToggle(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) NeonPurple_detail else GlassBg
    val textColor = if (isSelected) Color.White else TextWhite_detail.copy(alpha = 0.6f)
    val shadowModifier = if (isSelected) Modifier.shadow(8.dp, CircleShape, spotColor = NeonGlow) else Modifier

    Box(
        modifier = Modifier
            .size(42.dp)
            .then(shadowModifier)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.3f) else GlassBorder_detail,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GlassOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GlassBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextDim_detail)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite_detail.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextDim_detail.copy(alpha = 0.7f), fontSize = 12.sp)
        }

        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextWhite_detail.copy(alpha = 0.3f))
    }
}

@Composable
fun GlassSwitchItem(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GlassBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextDim_detail)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(title, color = TextWhite_detail.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

        // Custom Pill Switch
        AestheticPillSwitch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AestheticPillSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor = if (checked) NeonPurple_detail else Color(0xFF334155)
    val thumbColor = if (checked) Color.White else Color.White
    val alignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart

    // Shadow for glow effect when active
    val shadowMod = if(checked) Modifier.shadow(8.dp, RoundedCornerShape(15.dp), spotColor = NeonGlow) else Modifier

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .then(shadowMod)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        containerColor = SlateDark_detail,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK", color = NeonPurple_detail) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextDim_detail) }
        },
        text = { content() }
    )
}