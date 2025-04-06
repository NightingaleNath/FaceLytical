package com.codelytical.facelytical.ui.components

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.codelytical.facelytical.camera.CameraManager

/**
 * Displays camera preview using CameraX and handles lifecycle events
 */
@Composable
fun CameraPreview(
    cameraManager: CameraManager,
    modifier: Modifier = Modifier,
    onError: (Exception) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create and configure the preview view
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            // Create PreviewView
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Set implementation mode for best compatibility
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                // Use FILL_CENTER to maintain proper aspect ratio while filling the view
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            // Start camera when the view is ready
            cameraManager.startCamera(
                lifecycleOwner = lifecycleOwner,
                previewView = previewView.surfaceProvider,
                onError = onError
            )
        }
    )

    // Clean up camera resources when the composable is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraManager.shutdown()
        }
    }
}