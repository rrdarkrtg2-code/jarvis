package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisCyanBright
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisWarning

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ERROR
}

@Composable
fun JarvisCoreOrb(
    state: OrbState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")

    // Continuous rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 2000
                    OrbState.SPEAKING -> 4000
                    OrbState.LISTENING -> 5000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = when (state) {
        OrbState.ERROR -> JarvisError
        OrbState.THINKING -> JarvisWarning
        OrbState.EXECUTING -> JarvisBlue
        else -> JarvisCyan
    }

    val glowColor = primaryColor.copy(alpha = 0.25f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val baseRadius = (size.toPx() / 2) * 0.85f

            // Dynamic scale influenced by audio level (normalized 0..1)
            val dynamicScale = if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
                1.0f + (audioLevel.coerceIn(0f, 10f) / 40f)
            } else pulse

            val radius = baseRadius * dynamicScale

            // 1. Outer Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius * 1.3f
                ),
                radius = radius * 1.3f,
                center = center
            )

            // 2. Outer Rotating Sci-Fi Ring with segments
            rotate(rotation, center) {
                // Segment 1
                drawArc(
                    color = primaryColor,
                    startAngle = 10f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                // Segment 2
                drawArc(
                    color = primaryColor,
                    startAngle = 100f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                // Segment 3
                drawArc(
                    color = primaryColor,
                    startAngle = 190f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                // Segment 4
                drawArc(
                    color = primaryColor,
                    startAngle = 280f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Counter-rotating Middle Ring
            rotate(-rotation * 0.7f, center) {
                drawArc(
                    color = JarvisBlue.copy(alpha = 0.8f),
                    startAngle = 45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.75f, center.y - radius * 0.75f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.5f, radius * 1.5f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = JarvisBlue.copy(alpha = 0.8f),
                    startAngle = 225f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.75f, center.y - radius * 0.75f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.5f, radius * 1.5f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Inner Glowing Ring
            drawCircle(
                color = JarvisCyanBright.copy(alpha = 0.4f),
                radius = radius * 0.55f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 5. Core Disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color(0xFF0F172A)),
                    center = center,
                    radius = radius * 0.45f
                ),
                radius = radius * 0.45f,
                center = center
            )

            // 6. Iconic Futuristic Letter 'J' in the Center
            val jPath = Path().apply {
                val jW = radius * 0.35f
                val jH = radius * 0.55f
                val top = center.y - jH / 2
                val right = center.x + jW / 3
                val bottom = center.y + jH / 2
                val left = center.x - jW / 2

                moveTo(center.x, top)
                lineTo(right, top)
                lineTo(right, bottom - (jH * 0.25f))
                cubicTo(
                    right, bottom,
                    left, bottom,
                    left, bottom - (jH * 0.25f)
                )
                lineTo(left + (jW * 0.25f), bottom - (jH * 0.25f))
                cubicTo(
                    left + (jW * 0.25f), bottom - (jH * 0.1f),
                    right - (jW * 0.2f), bottom - (jH * 0.1f),
                    right - (jW * 0.2f), bottom - (jH * 0.25f)
                )
                lineTo(right - (jW * 0.2f), top + (jH * 0.15f))
                lineTo(center.x, top + (jH * 0.15f))
                close()
            }

            drawPath(
                path = jPath,
                brush = Brush.linearGradient(
                    colors = listOf(JarvisCyanBright, primaryColor),
                    start = Offset(center.x, center.y - radius * 0.3f),
                    end = Offset(center.x, center.y + radius * 0.3f)
                )
            )
        }
    }
}
