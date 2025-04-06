package com.codelytical.facelytical.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A circular capture button that shows a loading indicator when capturing
 * and a lock icon when disabled
 */
@Composable
fun CaptureButton(
    onCapture: () -> Unit,
    enabled: Boolean,
    isCapturing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animate opacity based on enabled state
    val alpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.4f)

    Box(
        modifier = modifier
            .scale(if (enabled) 1f else 0.95f)
            .alpha(alpha)
            .size(72.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .border(3.dp, Color.White, CircleShape)
            .clickable(enabled = enabled && !isCapturing) { onCapture() }
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCapturing) {
            // Show progress indicator when capturing
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        } else {
            // Show lock icon when face is not detected
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = if (enabled) Color.White else
                            Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = if (enabled) Color.White else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Lock icon when face is not detected
                if (!enabled) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Capture Disabled",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}