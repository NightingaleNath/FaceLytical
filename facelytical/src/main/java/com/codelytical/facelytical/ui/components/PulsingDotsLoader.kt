package com.codelytical.facelytical.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PulsingDotsLoader(
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White,
    dotSize: Dp = 6.dp,
    dotCount: Int = 3,
    spaceBetween: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingDots")

    // Create multiple animations with staggered delays
    val dots = List(dotCount) { index ->
        val delay = index * 100 // Staggered delay
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600,
                    easing = LinearEasing,
                    delayMillis = delay
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot$index"
        )

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600,
                    easing = LinearEasing,
                    delayMillis = delay
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dotAlpha$index"
        )

        scale to alpha
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { (scale, alpha) ->
            Box(
                modifier = Modifier
                    .size(dotSize * scale)
                    .background(
                        color = dotColor.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}