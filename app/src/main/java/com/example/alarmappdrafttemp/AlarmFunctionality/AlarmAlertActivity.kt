package com.example.alarmappdrafttemp.AlarmFunctionality

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Ensure screen stays on and allows interaction over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            AlarmAlertScreen(
                onDismiss = {
                    // Destroy the activity
                    val intent = Intent(this, AlarmService1::class.java).apply {
                        action = "ACTION_STOP_ALARM"
                    }
                    startService(intent)
                    finish()
                },
                onSnooze = {
                    val intent = Intent(this, AlarmService1::class.java).apply {
                        action = "ACTION_STOP_ALARM" // Or ACTION_SNOOZE if you implement it
                    }
                    startService(intent)
                    finish()
                }
            )
        }
    }
}

// --- HELPER: Find Activity from Context ---
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// --- CONSTANTS & COLORS ---
val NeonIndigo = Color(0xFF818CF8)
val DarkSlate = Color(0xFF0F172A)
val DeepBlack = Color(0xFF020617)
val GlassWhite = Color.White.copy(alpha = 0.08f)
val GlassBorder = Color.White.copy(alpha = 0.15f)

// --- UI COMPOSABLES ---

@Composable
fun AlarmAlertScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    // Real-time Clock Logic
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    // Background Gradient
    val bgBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1B4B), DarkSlate, DeepBlack)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // 1. Ambient Background Glows (Animated Blobs)
        AmbientGlows()

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // -- Top Status Bar (Mock) --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = timeFormat.format(currentTime), color = Color.White.copy(0.7f), fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SignalCellularAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.Wifi, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.BatteryFull, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // -- Center Content (Time & Icon) --
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // Bouncing Alarm Icon
                BouncingAlarmIcon()

                Spacer(modifier = Modifier.height(16.dp))

                // Time Display with Shimmer
                Row(verticalAlignment = Alignment.Bottom) {
                    ShimmerText(
                        text = timeFormat.format(currentTime),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.Thin
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = amPmFormat.format(currentTime),
                        color = NeonIndigo.copy(alpha = 0.9f),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Text(
                    text = dateFormat.format(currentTime),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "MORNING RUN",
                    color = NeonIndigo.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // -- Bottom Controls --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left: Snooze
                GlassActionButton(
                    icon = Icons.Default.Snooze,
                    label = "Snooze",
                    onClick = onSnooze
                )

                // Center: Visual Pulse (Decorative)
                PulseCenterVisual()

                // Right: Stop (Dismiss)
                GlassActionButton(
                    icon = Icons.Default.Close,
                    label = "Stop",
                    isDestructive = true,
                    onClick = onDismiss
                )
            }
        }

        // Bottom Handle Indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .width(100.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun AmbientGlows() {
    // Simulating the gradient blobs using Boxes with Radial Gradients
    // Purple Blob Top Left
    Box(
        modifier = Modifier
            .offset(x = (-100).dp, y = (-100).dp)
            .size(400.dp)
            .alpha(0.3f)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF9333EA), Color.Transparent)
                )
            )
    )
    // Blue Blob Bottom Right
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.BottomEnd)
            .offset(x = 100.dp, y = 100.dp)
            .size(400.dp)
            .alpha(0.25f)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2563EB), Color.Transparent)
                )
            )
    )
}

@Composable
fun BouncingAlarmIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val dy by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dy"
    )

    Icon(
        imageVector = Icons.Default.Alarm,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.5f),
        modifier = Modifier
            .size(32.dp)
            .offset(y = dy.dp)
    )
}

@Composable
fun ShimmerText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight
) {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.8f),
        Color.White,
        Color.White.copy(alpha = 0.8f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 3000, easing = LinearEasing),
            RepeatMode.Restart
        ), label = "translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
        end = Offset(translateAnim, translateAnim),
        tileMode = TileMode.Mirror
    )

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = TextStyle(
            brush = brush
        )
    )
}

@Composable
fun PulseCenterVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .padding(bottom = 24.dp) // Lift slightly to match text layout
    ) {
        // Outer Glow Ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .alpha(0.3f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, Color.Transparent)
                    )
                )
        )

        // Solid Center
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadowCustom(
                    color = Color.White.copy(alpha = 0.5f),
                    blurRadius = 30.dp
                )
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AlarmOff,
                contentDescription = null,
                tint = DarkSlate,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun GlassActionButton(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale = if (isPressed) 0.95f else 1f
    val bgAlpha = if (isPressed) 0.2f else 0.08f
    val iconTint = if (isDestructive && isPressed) Color(0xFFFCA5A5) else Color.White.copy(0.9f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = bgAlpha))
                .border(1.dp, if(isDestructive) Color.Red.copy(0.2f) else GlassBorder, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Custom ripple handled by scale
                    onClick = onClick
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

// Helper for shadow
fun Modifier.shadowCustom(
    color: Color = Color.Black,
    borderRadius: androidx.compose.ui.unit.Dp = 0.dp,
    blurRadius: androidx.compose.ui.unit.Dp = 0.dp,
    offsetY: androidx.compose.ui.unit.Dp = 0.dp,
    offsetX: androidx.compose.ui.unit.Dp = 0.dp,
    spread: androidx.compose.ui.unit.Dp = 0.dp,
) = this.then(
    Modifier.drawBehind {
        this.drawIntoCanvas {
            val paint = androidx.compose.ui.graphics.Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            val spreadPixel = spread.toPx()
            val leftPixel = (0f - spreadPixel) + offsetX.toPx()
            val topPixel = (0f - spreadPixel) + offsetY.toPx()
            val rightPixel = (this.size.width + spreadPixel)
            val bottomPixel = (this.size.height + spreadPixel)

            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter =
                    (android.graphics.BlurMaskFilter(blurRadius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL))
            }

            frameworkPaint.color = color.toArgb()
            it.drawRoundRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                radiusX = borderRadius.toPx(),
                radiusY = borderRadius.toPx(),
                paint
            )
        }
    }
)

// Needed imports for custom shadow and drawing


@Composable
@Preview
fun check(){
    AlarmAlertScreen(onSnooze = {}, onDismiss = {})
}