package com.example.alarmappdrafttemp.TimerFunctionality

import GlassBottomNavigation
import GlowFAB
import TransparentTimerBox
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint as ComposePaint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

// ==========================================
// 🎨 COLORS
// ==========================================
val NeonPurple = Color(0xFF818CF8)
val GlassWhite = Color.White.copy(alpha = 0.05f)
val GlassBorder = Color.White.copy(alpha = 0.1f)
val TextWhite = Color(0xFFF1F5F9)

// ==========================================
// 🛠️ HELPER FUNCTIONS
// ==========================================
fun millisToTimeParts(millis: Long): Triple<Int, Int, Int> {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return Triple(hours.toInt(), minutes.toInt(), seconds.toInt())
}

// ==========================================
// 📱 MAIN SCREEN COMPOSABLE
// ==========================================
@Composable
fun TimerScreen(
    onAlarmClick: () -> Unit,
    onStopwatchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: TimerViewModel
) {
    // UI State
    var selectionTab by rememberSaveable { mutableStateOf(false) }
    var blurEffect by rememberSaveable { mutableStateOf(0) }

    // Snackbar State
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Background Gradient (Dark Indigo -> Black)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color.Black)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .blur(blurEffect.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Ambient Background Blobs
        AmbientBackgroundBlobs()

        // 2. Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(20.dp))
                TimerHeader()
            }

            // Central Area: Always show the Circular UI
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularTimerDisplay(
                    viewModel = viewModel,
                    onShowSnackbar = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
            }

            // Spacer to lift content above the bottom nav
            Spacer(modifier = Modifier.height(100.dp))
        }

        // 3. Bottom Navigation
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            GlassBottomNavigation(
                currentTab = "Timer",
                onTimerClick = { },
                onAlarmClick = onAlarmClick,
                onStopwatchClick = onStopwatchClick,
                onSettingsClick = onSettingsClick
            )
        }

        // 4. FAB (Triggers Time Picker)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 116.dp)
        ) {
            GlowFAB(onClick = {
                if (!selectionTab) selectionTab = !selectionTab
            })
        }

        // 5. Snackbar Host (Positioned just above FAB)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 180.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }

    // 6. Time Picker Overlay
    if (selectionTab) {
        blurEffect = 20
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Eat clicks */ }
                )
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TransparentTimerBox(
                onCancel = { selectionTab = !selectionTab },
                viewModel = viewModel,
                onStart = { selectionTab = !selectionTab }
            )
        }
    } else {
        blurEffect = 0
    }
}

// ==========================================
// ⭕ CIRCULAR TIMER DISPLAY
// ==========================================
@Composable
fun CircularTimerDisplay(
    viewModel: TimerViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val timeLeft by viewModel.timerState.collectAsState()
    val totalTime by viewModel.timerTotal.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    // Calculate Progress (1.0 -> 0.0)
    val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime.toFloat() else 0f

    // Format Time Text
    val (h, m, s) = millisToTimeParts(timeLeft)
    val formattedTime = if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }

    // "Until XX:XX" Text Logic
    val etaText = remember(timeLeft, totalTime) {
        if (totalTime == 0L) "Set a timer"
        else {
            val eta = LocalTime.now().plusSeconds(timeLeft / 1000)
            "Until ${eta.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- THE GLOWING CIRCLE ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(320.dp)
        ) {
            // Pulse Animation for the background ring
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "pulseAlpha"
            )

            // Faint Background Ring
            Canvas(modifier = Modifier.fillMaxSize().scale(1.1f)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // The Main Neon Arc
            NeonProgressArc(
                progress = progress,
                modifier = Modifier.fillMaxSize().padding(20.dp)
            )

            // Center Text Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    style = TextStyle(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        letterSpacing = (-2).sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = NeonPurple.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = etaText,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonPurple.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // --- CONTROL BUTTONS ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // 1. Cancel / Reset Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { viewModel.resetTimer() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Pause / Resume Button
            // We use a dedicated Box for the Glow to avoid Polygon artifacts
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp) // Large container to hold the spread glow
            ) {
                // LAYER 1: The Soft Blur Glow (Animated)
                if (totalTime > 0) {
                    val pulseTransition = rememberInfiniteTransition(label = "pulse_glow")
                    val glowScale by pulseTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            tween(1500, easing = LinearEasing),
                            RepeatMode.Reverse
                        ),
                        label = "glowScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(glowScale)
                            .background(NeonPurple.copy(alpha = 0.6f), CircleShape)
                            .blur(radius = 24.dp) // <--- THIS CREATES THE SMOOTH GLOW
                    )
                }

                // LAYER 2: The Actual Clickable Button (Sharp Edges)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(NeonPurple.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, NeonPurple.copy(alpha = 0.5f), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            if (totalTime == 0L) {
                                onShowSnackbar("Please choose a time first")
                            } else {
                                if (isRunning) viewModel.pauseTimer() else viewModel.resumeTimer()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 🌈 NEON ARC COMPONENT
// ==========================================
@Composable
fun NeonProgressArc(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val diameter = size.minDimension

        // Correct Radius Calculation
        val radius = (diameter - strokeWidth) / 2

        val cx = size.width / 2
        val cy = size.height / 2

        // 1. Background Track
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 4.dp.toPx())
        )

        val sweepAngle = progress * 360f

        // 2. Neon Glow (Blurred Stroke)
        drawIntoCanvas {
            val paint = Paint().apply {
                color = NeonPurple.toArgb()
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth + 20f
                strokeCap = Paint.Cap.ROUND
                maskFilter = android.graphics.BlurMaskFilter(30f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                alpha = 150
            }
            it.nativeCanvas.drawArc(
                cx - radius,
                cy - radius,
                cx + radius,
                cy + radius,
                -90f,
                sweepAngle,
                false,
                paint
            )
        }

        // 3. Solid Core Arc
        drawArc(
            color = NeonPurple,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 4. White Dot Tip
        // Logic: Only draw if progress > 0 to avoid a dot at 12 o'clock on an empty timer
        if (progress > 0.001f) {
            val angleInRad = Math.toRadians((sweepAngle - 90).toDouble())
            val dotX = cx + radius * cos(angleInRad).toFloat()
            val dotY = cy + radius * sin(angleInRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth / 1.5f,
                center = Offset(dotX, dotY)
            )
        }
    }
}

// ==========================================
// 🧩 HEADER COMPONENT
// ==========================================
@Composable
fun TimerHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Timer",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )
        )
        IconButton(
            onClick = { },
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = TextWhite)
        }
    }
}

// ==========================================
// 🌌 BACKGROUND BLOBS COMPONENT
// ==========================================
@Composable
fun AmbientBackgroundBlobs() {
    val infiniteTransition = rememberInfiniteTransition(label = "blob_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Top Left (Purple)
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .size(300.dp)
                .scale(pulseScale)
                .background(Color(0xFF9333EA).copy(alpha = 0.3f), CircleShape)
                .blur(80.dp)
        )
        // Top Right (Indigo)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 100.dp)
                .size(280.dp)
                .scale(pulseScale)
                .background(Color(0xFF6366F1).copy(alpha = 0.2f), CircleShape)
                .blur(80.dp)
        )
        // Bottom Left (Blue)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 20.dp, y = 50.dp)
                .size(320.dp)
                .background(Color(0xFF2563EB).copy(alpha = 0.2f), CircleShape)
                .blur(80.dp)
        )
    }
}