
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.* // Using Rounded icons for the aesthetic look
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmappdrafttemp.DB.AlarmItem
import com.example.alarmappdrafttemp.AlarmFunctionality.AlarmViewModel
import java.time.format.DateTimeFormatter

// --- Aesthetic Color Palette ---
val DeepIndigo = Color(0xFF312E81)
val SlateDark = Color(0xFF0F172A)
val RichBlack = Color(0xFF050505)
val NeonPurple = Color(0xFF818CF8)
val GlassWhite = Color.White.copy(alpha = 0.05f)
val GlassBorder = Color.White.copy(alpha = 0.1f)
val TextWhite = Color(0xFFF1F5F9)
val TextDim = Color(0xFF94A3B8)

// --- Data Model (Preserved) ---
data class Alarm(
    val obj: AlarmItem,
    val time: String,
    val amPm: String,
    val label: String,
    val schedule: String,
    var isEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    onsaveClick: () -> Unit = {},
    viewModel: AlarmViewModel,
    onTimerClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onStopwatchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()
    var isTimerSelected ="Alarm"


    // 1. Background Gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DeepIndigo, SlateDark, RichBlack)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent, // Let gradient show through
            bottomBar = { GlassBottomNavigation(isTimerSelected,
                onTimerClick,
                onAlarmClick,
                onStopwatchClick,
                onSettingsClick) },
            floatingActionButton = {
                GlowFAB(onClick = onsaveClick)
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Header
                Spacer(modifier = Modifier.height(20.dp))
                AlarmHeader()
                Spacer(modifier = Modifier.height(24.dp))

                // List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(alarms) { alarmEntity ->
                        val formatter1 = DateTimeFormatter.ofPattern("h:mm")
                        val formatter2 = DateTimeFormatter.ofPattern("a")
                        val formattedTime = alarmEntity.alarmitem.time.format(formatter1)
                        val formattedTimeAmPm = alarmEntity.alarmitem.time.format(formatter2)

                        // Pass data to the Glass Card
                        GlassAlarmCard(
                            alarm = Alarm(
                                obj = alarmEntity.alarmitem,
                                time = formattedTime,
                                amPm = formattedTimeAmPm,
                                label = alarmEntity.alarmitem.message ?: "Alarm",
                                schedule = "Everyday", // Placeholder or fetch from DB
                                isEnabled = alarmEntity.alarmitem.turnedOn
                            ),
                            id = alarmEntity.id,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassAlarmCard(alarm: Alarm, id: Int, viewModel: AlarmViewModel) {
    // Local state for smooth UI toggling
    var isEnabled by remember(alarm.isEnabled) { mutableStateOf(alarm.isEnabled) }
    val opacity = if (isEnabled) 1f else 0.6f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = opacity }
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { /* Expand/Edit Logic */ },
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top Row: Time and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.time,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light,
                        color = if (isEnabled) TextWhite else TextWhite.copy(alpha = 0.7f),
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alarm.amPm,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDim,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Custom Aesthetic Switch connected to ViewModel
                AestheticSwitch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        // Call ViewModel exactly as requested
                        viewModel.scheduleAlarm(
                            AlarmItem(
                                alarm.obj.time,
                                alarm.obj.message,
                                checked,
                                alarm.obj.days

                            ),
                            id
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Row: Label and Expand Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if(alarm.label.isNotEmpty()) alarm.label else "Alarm",
                        color = TextWhite.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = alarm.schedule,
                        color = TextDim.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Expand",
                    tint = TextWhite.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// --- Components ---

@Composable
fun AestheticSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackColor = if (checked) NeonPurple else Color(0xFF334155)
    val thumbColor = if (checked) Color.White else Color.White.copy(alpha = 0.5f)
    val alignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) } // Toggle on click
            .padding(4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun AlarmHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Alarm",
                color = TextWhite,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Next in 4h 20m", // Dynamic text logic can be added here
                color = NeonPurple.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
        IconButton(
            onClick = { /* Settings */ },
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Settings", tint = TextWhite)
        }
    }
}

@Composable
fun GlowFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .offset(y = 45.dp)
            .size(64.dp)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = NeonPurple,
                ambientColor = NeonPurple
            )
            .clip(RoundedCornerShape(18.dp))
            .background(NeonPurple)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun GlassBottomNavigation(
    currentTab :String,
    onTimerClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onStopwatchClick: () -> Unit,
    onSettingsClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.8f),
                        Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("Alarm", Icons.Rounded.Alarm, currentTab=="Alarm",{
                onAlarmClick()

            })
            NavItem("Timer", Icons.Rounded.HourglassEmpty, currentTab=="Timer", {
                onTimerClick()
            })
            Spacer(modifier = Modifier.width(48.dp)) // Gap for FAB
            NavItem("Stopwatch", Icons.Rounded.Watch, false,onStopwatchClick)
            NavItem("Settings", Icons.Rounded.Settings, false,onSettingsClick)
        }
    }
}



@Composable
fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit // <--- 1. Add this parameter
) {
    val color = if (isSelected) NeonPurple else TextDim.copy(alpha = 0.5f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick,) // <--- 2. Bind the click here
            .padding(8.dp) // Optional: Increases touch target size for better UX
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NeonPurple.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.align(Alignment.Center).size(26.dp)
                )
            }
        } else {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (isSelected) Color.White else color)
    }
}
