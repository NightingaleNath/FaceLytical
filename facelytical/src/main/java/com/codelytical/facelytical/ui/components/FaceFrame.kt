package com.codelytical.facelytical.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * The face frame overlay displayed on top of camera preview
 */
@Composable
fun FaceFrame(
    modifier: Modifier = Modifier,
    isFaceDetected: Boolean
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
    ) {
        val strokeWidth = 5.dp.toPx()
        val cornerRadius = 20.dp.toPx()
        val cornerLength = 40.dp.toPx()

        // The rectangle that defines the frame
        val rect = Rect(
            left = strokeWidth,
            top = strokeWidth,
            right = size.width - strokeWidth,
            bottom = size.height - strokeWidth
        )

        // Change color based on face detection
        val frameColor = if (isFaceDetected) Color.Green else Color.White

        // Draw the top-left corner
        drawLine(
            color = frameColor,
            start = Offset(rect.left, rect.top + cornerLength),
            end = Offset(rect.left, rect.top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = frameColor,
            start = Offset(rect.left, rect.top),
            end = Offset(rect.left + cornerLength, rect.top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw the top-right corner
        drawLine(
            color = frameColor,
            start = Offset(rect.right - cornerLength, rect.top),
            end = Offset(rect.right, rect.top),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = frameColor,
            start = Offset(rect.right, rect.top),
            end = Offset(rect.right, rect.top + cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw the bottom-left corner
        drawLine(
            color = frameColor,
            start = Offset(rect.left, rect.bottom - cornerLength),
            end = Offset(rect.left, rect.bottom),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = frameColor,
            start = Offset(rect.left, rect.bottom),
            end = Offset(rect.left + cornerLength, rect.bottom),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw the bottom-right corner
        drawLine(
            color = frameColor,
            start = Offset(rect.right - cornerLength, rect.bottom),
            end = Offset(rect.right, rect.bottom),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = frameColor,
            start = Offset(rect.right, rect.bottom),
            end = Offset(rect.right, rect.bottom - cornerLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}