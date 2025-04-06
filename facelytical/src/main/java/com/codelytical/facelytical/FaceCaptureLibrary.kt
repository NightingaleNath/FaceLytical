package com.codelytical.facelytical

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.codelytical.facelytical.ui.FaceCaptureScreen

/**
 * Main library class that provides face capture functionality
 */
class FaceCaptureLibrary {
    companion object {
        /**
         * Provides the face capture screen to be used in any app
         *
         * @param onImageCaptured Callback function when an image is successfully captured
         * @param onCaptureError Callback function when an error occurs during capture
         * @param onPermissionDenied Callback function when camera permission is denied
         * @param onCustomerSupportClick Callback function when customer support is clicked
         * @param onBackPressed Callback function when back button is pressed
         */
        @Composable
        fun FaceCaptureView(
            onImageCaptured: (Bitmap) -> Unit,
            onCaptureError: (Exception) -> Unit,
            onPermissionDenied: () -> Unit,
            onBackPressed: () -> Unit = {}
        ) {
            FaceCaptureScreen(
                onImageCaptured = onImageCaptured,
                onCaptureError = onCaptureError,
                onPermissionDenied = onPermissionDenied,
                onBackPressed = onBackPressed
            )
        }
    }
}