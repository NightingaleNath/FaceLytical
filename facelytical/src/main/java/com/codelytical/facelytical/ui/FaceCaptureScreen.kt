package com.codelytical.facelytical.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.codelytical.facelytical.camera.CameraManager
import com.codelytical.facelytical.permissions.CameraPermissionHandler
import com.codelytical.facelytical.ui.components.CameraPreview
import com.codelytical.facelytical.ui.components.CaptureButton
import com.codelytical.facelytical.ui.components.FaceFrame
import com.codelytical.facelytical.utils.ErrorHandler
import com.codelytical.facelytical.utils.Instructions

/**
 * Main screen for face capture functionality
 */
@Composable
fun FaceCaptureScreen(
    onImageCaptured: (Bitmap) -> Unit,
    onCaptureError: (Exception) -> Unit,
    onPermissionDenied: () -> Unit,
    onBackPressed: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }

    var isCapturing by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showWalkthrough by remember { mutableStateOf(false) }

    // Get face detection state
    val isFaceDetected by cameraManager.isFaceDetected.collectAsState(initial = false)

    val instructionText = when {
        isCapturing -> Instructions.CAPTURING
        isFaceDetected -> Instructions.FACE_DETECTED
        else -> Instructions.POSITION_FACE
    }

    // Calculate dimensions for the camera preview
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Define a square frame for the face capture - adjust based on screen height
    val previewSizePercent = if (screenHeight < 600.dp) 0.75f else 0.85f
    val previewSize = screenWidth * previewSizePercent

    // Define background color
    val backgroundColor = Color(0xFF0F172A)

    // Show walkthrough dialog if requested
    if (showWalkthrough) {
        FaceWalkthroughDialog(onDismiss = { showWalkthrough = false })
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .zIndex(0f)
        )

        // Main content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Camera permission handler
            CameraPermissionHandler(
                onPermissionGranted = { hasPermission = true },
                onPermissionDenied = {
                    hasPermission = false
                    onPermissionDenied()
                }
            ) {
                if (hasPermission) {
                    // Content when permission is granted
                    // Add spacer at top to push content down slightly
                    Spacer(modifier = Modifier.height(20.dp))

                    // Main content with camera and controls
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top section with camera preview
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            // Camera view constrained to square box
                            Box(
                                modifier = Modifier
                                    .width(previewSize)
                                    .aspectRatio(1f)
                                    .clip(RectangleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isFaceDetected) Color.Green else Color.White,
                                        shape = RectangleShape
                                    )
                            ) {
                                // Camera preview
                                CameraPreview(
                                    cameraManager = cameraManager,
                                    onError = { error ->
                                        errorMessage = ErrorHandler.handleCameraError(error)
                                        onCaptureError(error)
                                    }
                                )

                                // Face frame
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FaceFrame(isFaceDetected = isFaceDetected)
                                }
                            }
                        }

                        // Bottom section with controls and text
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            backgroundColor.copy(alpha = 0.0f),
                                            backgroundColor.copy(alpha = 0.85f)
                                        )
                                    )
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Instruction text
                            Text(
                                text = instructionText,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    bottom = 10.dp,
                                    top = 20.dp
                                )
                            )

                            // Control buttons row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Close button (left)
                                Box(
                                    modifier = Modifier
                                        .padding(start = 20.dp)
                                        .size(32.dp)
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = CircleShape
                                        )
                                        .background(
                                            color = Color.Red.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            cameraManager.shutdown()
                                            onBackPressed()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Capture button (center)
                                CaptureButton(
                                    onCapture = {
                                        isCapturing = true
                                        // Pause face detection during capture
                                        cameraManager.pauseFaceDetection()

                                        cameraManager.captureImage(
                                            onImageCaptured = { bitmap ->
                                                isCapturing = false
                                                // Resume face detection
                                                cameraManager.resumeFaceDetection()
                                                // Stop camera before returning to third-party app
                                                cameraManager.shutdown()
                                                onImageCaptured(bitmap)
                                            },
                                            onError = { error ->
                                                isCapturing = false
                                                // Resume face detection
                                                cameraManager.resumeFaceDetection()

                                                // Check if this is a "No face detected" error
                                                if (error.message?.contains("No face detected") == true) {
                                                    // Show toast instead of passing error back
                                                    Toast.makeText(
                                                        context,
                                                        "No face detected. Please keep your face in the frame.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    // Handle other errors normally
                                                    errorMessage = ErrorHandler.handleCameraError(error)
                                                    onCaptureError(error)
                                                }
                                            }
                                        )
                                    },
                                    enabled = isFaceDetected && !isCapturing,
                                    isCapturing = isCapturing
                                )

                                // Help button (right)
                                Box(
                                    modifier = Modifier
                                        .padding(end = 20.dp)
                                        .size(32.dp)
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = CircleShape
                                        )
                                        .background(
                                            color = Color(0xFF3B82F6),
                                            shape = CircleShape
                                        )
                                        .clickable { showWalkthrough = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QuestionMark,
                                        contentDescription = "Help",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Bottom padding
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                } else {
                    // Permission denied view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "This app needs camera permission to detect faces and take photos.",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}