import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmappdrafttemp.TimerFunctionality.TimerViewModel
import java.util.concurrent.TimeUnit




val CardColor = Color(0xFF242753)

// ==========================================
// 🎛️ CONTROL CENTER - CHANGE TRANSPARENCY HERE
// ==========================================

data class GlassSettings(
    val containerAlpha: Float = 1f,
    val borderAlpha: Float = 0.20f,
    val cornerRadius: Dp = 24.dp
)

val currentSettings = GlassSettings()

// ==========================================
// 🎨 COLORS
// ==========================================
val BaseColor = CardColor
val BorderColor = Color.White
val NeonAccent = Color(0xFFBA74FF)

fun toMillis(hours: Int, minutes: Int): Long {
    return TimeUnit.HOURS.toMillis(hours.toLong()) +
            TimeUnit.MINUTES.toMillis(minutes.toLong())
}

fun millisToHourMinute(millis: Long): Pair<Int, Int> {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return hours.toInt() to minutes.toInt()
}

@Composable
fun TransparentTimerBox(
    onCancel: () -> Unit,
    viewModel: TimerViewModel,
    onStart: () -> Unit
) {
    // Stores the input digits as a string (e.g., "130" = 1 min 30 sec)
    // Max length 6 digits (HHMMSS)
    var inputString by remember { mutableStateOf("") }

    // Helper to parse the raw string into readable time units
    fun parseInput(): Triple<Int, Int, Int> {
        val padded = inputString.padStart(6, '0')
        val h = padded.substring(0, 2).toInt()
        val m = padded.substring(2, 4).toInt()
        val s = padded.substring(4, 6).toInt()
        return Triple(h, m, s)
    }

    // Helper to handle button clicks
    fun onKeyPadClick(key: String) {
        if (key == "DEL") {
            if (inputString.isNotEmpty()) {
                inputString = inputString.dropLast(1)
            }
        } else {
            // Limit to 6 digits (99h 99m 99s theoretically, logic handles overflow)
            if (inputString.length < 6) {
                // Prevent leading zeros unless it's the only digit (logic naturally handles this by int conversion)
                inputString += key
            }
        }
    }

    Box(
        modifier = Modifier
            .width(340.dp) // Slightly wider for keypad comfort
            .clip(RoundedCornerShape(currentSettings.cornerRadius))
            .background(BaseColor.copy(alpha = currentSettings.containerAlpha))
            .border(
                width = 1.dp,
                color = BorderColor.copy(alpha = currentSettings.borderAlpha),
                shape = RoundedCornerShape(currentSettings.cornerRadius)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. Header ---
            Text(
                text = "Set Timer",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. Digital Display ---
            val (h, m, s) = parseInput()

            // Format logic: Gray out zeros that haven't been entered yet
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hours
                TimeUnitDisplay(value = h, unit = "h", isActive = inputString.length > 4)
                Spacer(modifier = Modifier.width(8.dp))
                // Minutes
                TimeUnitDisplay(value = m, unit = "m", isActive = inputString.length > 2)
                Spacer(modifier = Modifier.width(8.dp))
                // Seconds (Always active slightly for visibility, or active if length > 0)
                TimeUnitDisplay(value = s, unit = "s", isActive = true, isEmpty = inputString.isEmpty())
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. Keypad ---
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton("1") { onKeyPadClick("1") }
                    KeypadButton("2") { onKeyPadClick("2") }
                    KeypadButton("3") { onKeyPadClick("3") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton("4") { onKeyPadClick("4") }
                    KeypadButton("5") { onKeyPadClick("5") }
                    KeypadButton("6") { onKeyPadClick("6") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton("7") { onKeyPadClick("7") }
                    KeypadButton("8") { onKeyPadClick("8") }
                    KeypadButton("9") { onKeyPadClick("9") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Empty spacer for alignment
                    Spacer(modifier = Modifier.size(60.dp))
                    KeypadButton("0") { onKeyPadClick("0") }
                    // Backspace Button
                    IconButton(
                        onClick = { onKeyPadClick("DEL") },
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Backspace,
                            contentDescription = "Delete",
                            tint = NeonAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. Action Buttons ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Cancel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                }

                // Start
                Button(
                    onClick = {
                        val duration = TimeUnit.HOURS.toMillis(h.toLong()) +
                                TimeUnit.MINUTES.toMillis(m.toLong()) +
                                TimeUnit.SECONDS.toMillis(s.toLong())

                        if (duration > 0) {
                            viewModel.startTimer(duration)
                            onStart()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TimeUnitDisplay(value: Int, unit: String, isActive: Boolean, isEmpty: Boolean = false) {
    val textAlpha = if (isEmpty) 0.3f else if (isActive) 1f else 0.5f
    val unitAlpha = if (isActive) 0.8f else 0.4f

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = String.format("%02d", value),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = textAlpha)
        )
        Text(
            text = unit,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = NeonAccent.copy(alpha = unitAlpha),
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
        )
    }
}

@Composable
fun KeypadButton(number: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}