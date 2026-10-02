package com.example.alarmappdrafttemp.StopWatchFunctionality

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

// --- 1. CONFIGURATION ---
data class TransparencyConfig(
    val panelAlpha: Float = 0.05f,
    val borderAlpha: Float = 0.15f, // Slightly increased for visibility on stadium
    val textPrimaryAlpha: Float = 1.0f,
    val textSecondaryAlpha: Float = 0.7f,
    val textTertiaryAlpha: Float = 0.4f
)

val defaultTransparency = TransparencyConfig()

// --- 2. COLORS ---
val DeepIndigo = Color(0xFF312E81)
val SlateDark = Color(0xFF0F172A)
val RichBlack = Color(0xFF050505)
val ColorPrimary = Color(0xFF818CF8)
val ColorGlassBorder = Color.White
val ColorGlassSurface = Color.White
val TextDim = Color(0xFF94A3B8)

// --- 3. MAIN SCREEN ---
@Composable
fun GlassStopwatchScreen(
    viewModel: StopwatchViewModel = koinViewModel(),
    config: TransparencyConfig = defaultTransparency,
    onTimerClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    fun formatTime(millis: Long): Pair<String, String> {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        val hundredths = (millis % 1000) / 10
        val mainText = String.format("%02d:%02d", minutes, seconds)
        val smallText = String.format(".%02d", hundredths)
        return mainText to smallText
    }

    val (mainTimeText, smallTimeText) = formatTime(state.TimeElapsed)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DeepIndigo, SlateDark, RichBlack)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        AmbientBackground()

        Scaffold(
            containerColor = Color.Transparent,
            // UNCHANGED: Bottom Nav
            bottomBar = {
                GlassBottomNavigation(
                    currentTab = "Stopwatch",
                    onTimerClick = onTimerClick,
                    onAlarmClick = onAlarmClick,
                    onStopwatchClick = { },
                    onSettingsClick = onSettingsClick
                )
            },
            // UNCHANGED: Glow FAB
            floatingActionButton = {
                GlowFAB(
                    onClick = { viewModel.toggleStartPause() },
                    icon = if (state.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                HeaderSection(config)

                Spacer(modifier = Modifier.height(12.dp))

                // UPDATED: Stadium Timer Display (Saves vertical space)
                StadiumTimerDisplay(config, mainTimeText, smallTimeText)

                Spacer(modifier = Modifier.height(24.dp))

                // Controls (Reset / Lap)
                ControlButtons(
                    config = config,
                    onReset = { viewModel.reset() },
                    onLap = { viewModel.lap() },
                    isRunning = state.isRunning,
                    hasTime = state.TimeElapsed > 0
                )

                Spacer(modifier = Modifier.height(24.dp))

                LapHeader(config)

                // UPDATED: Lap List with Weight (Fills remaining space)
                LapList(config, state.Laps)
            }
        }
    }
}

// --- 4. NEW & UPDATED COMPONENTS ---

@Composable
fun StadiumTimerDisplay(config: TransparencyConfig, mainText: String, smallText: String) {
    // The "Stadium" shape from your HTML design
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Much shorter than the 280dp circle
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(40), // Large rounding for stadium effect
                spotColor = ColorPrimary.copy(alpha = 0.2f)
            )
            .glassEffect(config, shape = RoundedCornerShape(40))
            // Inner glow simulation using a border
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(40)
            )
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = mainText,
                fontSize = 64.sp, // Larger text for better visibility in rectangle
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = config.textPrimaryAlpha),
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = smallText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp) // Align baselines
            )
        }
    }
}

@Composable
fun LapList(config: TransparencyConfig, laps: List<LapData>) {
    // Added weight(1f) to ensure this takes up all remaining space
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            ,
        contentPadding = PaddingValues(bottom = 120.dp), // Padding for Bottom Nav
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(laps) { index, lap ->
            val minutes = (lap.splitTime / 1000) / 60
            val seconds = (lap.splitTime / 1000) % 60
            val hundredths = (lap.splitTime % 1000) / 10
            val timeString = String.format("%02d:%02d.%02d", minutes, seconds, hundredths)

            val isLatest = index == 0
            val itemAlpha = if (isLatest) 1f else (0.9f - (index * 0.05f)).coerceAtLeast(0.5f)

            // Make rows slightly slimmer to fit more
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                config = config,
                highlight = isLatest,
                contentAlpha = itemAlpha
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lap ${lap.LapNumber}",
                        color = if (isLatest) ColorPrimary else Color(0xFF94A3B8),
                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = timeString,
                        color = Color.White.copy(alpha = if (isLatest) 1f else 0.8f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Light
                    )
                }
            }
        }
    }
}

// --- 5. UNCHANGED COMPONENTS (Preserved exactly as requested) ---

@Composable
fun HeaderSection(config: TransparencyConfig) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Stopwatch",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = config.textPrimaryAlpha),
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-1).sp
        )
    }
}

@Composable
fun ControlButtons(
    config: TransparencyConfig,
    onReset: () -> Unit,
    onLap: () -> Unit,
    isRunning: Boolean,
    hasTime: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp) // Adjusted padding
    ) {
        // RESET Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .glassEffect(config, shape = CircleShape)
                .clickable { onReset() }
        ) {
            Text(
                text = "Reset",
                color = Color.White.copy(alpha = config.textSecondaryAlpha),
                fontWeight = FontWeight.Medium
            )
        }

        // LAP Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .glassEffect(config, shape = CircleShape)
                .clickable { onLap() }
        ) {
            Text(
                text = "Lap",
                color = Color.White.copy(alpha = config.textSecondaryAlpha),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LapHeader(config: TransparencyConfig) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "LAP NO.",
            color = Color.White.copy(alpha = config.textTertiaryAlpha),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "SPLIT TIME",
            color = Color.White.copy(alpha = config.textTertiaryAlpha),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun GlowFAB(onClick: () -> Unit, icon: ImageVector) {
    Box(
        modifier = Modifier
            .offset(y = 45.dp)
            .size(64.dp)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = ColorPrimary,
                ambientColor = ColorPrimary
            )
            .clip(RoundedCornerShape(18.dp))
            .background(ColorPrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Action",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun GlassBottomNavigation(
    currentTab: String,
    onTimerClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onStopwatchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
            NavItem("Alarm", Icons.Rounded.Alarm, currentTab == "Alarm", onAlarmClick)
            NavItem("Timer", Icons.Rounded.HourglassEmpty, currentTab == "Timer", onTimerClick)
            Spacer(modifier = Modifier.width(48.dp))
            NavItem("Stopwatch", Icons.Rounded.Watch, currentTab == "Stopwatch", onStopwatchClick)
            NavItem("Settings", Icons.Rounded.Settings, currentTab == "Settings", onSettingsClick)
        }
    }
}

@Composable
fun NavItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) ColorPrimary else TextDim.copy(alpha = 0.5f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ColorPrimary.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(26.dp)
                )
            }
        } else {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (isSelected) Color.White else color)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    config: TransparencyConfig,
    highlight: Boolean = false,
    contentAlpha: Float = 1f,
    content: @Composable () -> Unit
) {
    val borderModifier = if (highlight) {
        Modifier.border(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    ColorPrimary.copy(alpha = 0.5f),
                    Color.Transparent
                )
            ),
            shape = RoundedCornerShape(16.dp)
        )
    } else {
        Modifier.border(
            1.dp,
            Color.White.copy(alpha = config.borderAlpha),
            RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .background(
                ColorGlassSurface.copy(alpha = config.panelAlpha),
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        if (highlight) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(ColorPrimary)
            )
        }
        Box(modifier = Modifier.alpha(contentAlpha)) {
            content()
        }
    }
}

fun Modifier.glassEffect(config: TransparencyConfig, shape: androidx.compose.ui.graphics.Shape) =
    this
        .background(ColorGlassSurface.copy(alpha = config.panelAlpha), shape)
        .border(1.dp, ColorGlassBorder.copy(alpha = config.borderAlpha), shape)
        .clip(shape)

fun Modifier.neonGlow(color: Color, radius: Dp = 15.dp) = this.drawBehind {
    this.drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.color = color
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.maskFilter = BlurMaskFilter(
            radius.toPx(),
            BlurMaskFilter.Blur.NORMAL
        )
        canvas.drawCircle(
            center = Offset(size.width / 2, size.height / 2),
            radius = size.width / 2,
            paint = paint
        )
    }
}

@Composable
fun AmbientBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF9333EA).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(100f, 100f),
                radius = 500f
            ),
            center = Offset(0f, 0f),
            radius = 600f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(size.width, size.height * 0.3f),
                radius = 500f
            ),
            center = Offset(size.width, size.height * 0.3f),
            radius = 500f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(0f, size.height),
                radius = 600f
            ),
            center = Offset(100f, size.height),
            radius = 600f
        )
    }
}