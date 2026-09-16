package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisCyan

@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val barCount = 32
        val step = width / barCount

        val amplitudeMultiplier = if (isActive) {
            (15f + audioLevel.coerceIn(0f, 10f) * 2f).coerceAtMost(height / 2 - 2)
        } else 4f

        for (i in 0 until barCount) {
            val x = i * step + step / 2
            val wave = Math.sin((i.toDouble() / barCount * 4 * Math.PI) + phase).toFloat()
            val barHeight = Math.abs(wave) * amplitudeMultiplier + 2f

            drawLine(
                color = JarvisCyan.copy(alpha = if (isActive) 0.85f else 0.3f),
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
