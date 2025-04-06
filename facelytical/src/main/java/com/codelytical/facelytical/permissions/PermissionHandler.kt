package com.codelytical.facelytical.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Simple permission state handler
 */
class PermissionState(
    val permission: String,
    val hasPermission: Boolean,
    val launchRequest: () -> Unit
)

/**
 * Creates and remembers a permission state for the camera
 */
@Composable
fun rememberCameraPermissionState(): PermissionState {
    val context = LocalContext.current

    // Track if permission request has been sent
    val permissionRequestSent = remember { mutableStateOf(false) }

    // Check current permission status
    val hasPermission = remember {
        checkPermission(context, Manifest.permission.CAMERA)
    }

    // Permission request launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Result handled by rechecking permission state */ }

    return remember {
        PermissionState(
            permission = Manifest.permission.CAMERA,
            hasPermission = hasPermission,
            launchRequest = {
                if (!permissionRequestSent.value) {
                    permissionRequestSent.value = true
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }
}

/**
 * Checks if a permission is granted
 */
fun checkPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Simple composable that handles permission checking and requesting
 */
@Composable
fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberCameraPermissionState()

    // Update permission state whenever it changes
    val hasPermission = remember(permissionState) {
        checkPermission(context, Manifest.permission.CAMERA)
    }

    // Request permission if needed
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionState.launchRequest()
        }
    }

    // Check permission status and call appropriate callback
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            onPermissionGranted()
        } else if (permissionState.launchRequest != null) {
            onPermissionDenied()
        }
    }

    content()
}