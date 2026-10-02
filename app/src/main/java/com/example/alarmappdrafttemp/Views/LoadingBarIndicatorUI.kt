import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun Grid3x3Loader(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp, // Control the overall size here
    color: Color = Color(0xFF2196F3),
    duration: Int = 1500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "grid_loader")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.size(size)) {
        // Dynamic Calculation:
        // Divide the canvas width by 3 to get the size of one "cell"
        val cellWidth = this.size.width / 3
        val centerOffset = cellWidth / 2

        // Make dot radius relative to the cell (e.g., 25% of the cell width)
        // This ensures dots are always proportional to the overall size.
        val dotRadius = cellWidth * 0.25f

        for (row in 0..2) {
            for (col in 0..2) {
                val x = (col * cellWidth) + centerOffset
                val y = (row * cellWidth) + centerOffset

                val diagonalIndex = row + col
                val wave = sin(time - (diagonalIndex * 0.6f))
                val opacity = (wave + 1f) / 2f * 0.8f + 0.2f

                drawCircle(
                    color = color.copy(alpha = opacity),
                    radius = dotRadius, // Uses the dynamic radius
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Preview
@Composable
fun ScalableLoaderPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Tiny Version (Icon size)
        Grid3x3Loader(size = 24.dp)

        // Standard Version
        Grid3x3Loader(size = 64.dp)

        // Massive Version
        Grid3x3Loader(size = 150.dp)
    }
}