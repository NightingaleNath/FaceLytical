// utils/ErrorHandler.kt
package com.codelytical.facelytical.utils

import android.util.Log

object ErrorHandler {
    private const val TAG = "FaceCaptureLibrary"

    fun handleCameraError(exception: Exception): String {
        Log.e(TAG, "Camera error: ${exception.message}", exception)
        return when (exception) {
            is androidx.camera.core.ImageCaptureException -> {
                "Failed to capture image: ${exception.message}"
            }
            is SecurityException -> {
                "Camera permission denied"
            }
            else -> {
                "An unexpected error occurred: ${exception.message}"
            }
        }
    }
}