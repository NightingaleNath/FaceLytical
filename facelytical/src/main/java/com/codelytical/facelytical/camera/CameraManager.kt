package com.codelytical.facelytical.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.codelytical.facelytical.utils.ImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manages camera operations including setup, face detection, and image capture
 */
class CameraManager(private val context: Context) {
    private val TAG = "CameraManager"

    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var previewView: Preview.SurfaceProvider? = null
    private var preview: Preview? = null
    private var faceDetectionAnalyzer: FaceDetectionAnalyzer? = null
    private var isFaceDetectionPaused = false

    // Face detection state
    private val _isFaceDetected = MutableStateFlow(false)
    val isFaceDetected: StateFlow<Boolean> = _isFaceDetected.asStateFlow()

    // Store current face bounds
    private var currentFaceBounds: Rect? = null

    // Face detector for secondary verification
    private val faceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.15f)
        .build()

    private val faceDetector = FaceDetection.getClient(faceDetectorOptions)

    /**
     * Set up and start the camera with preview, analysis and capture use cases
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: Preview.SurfaceProvider,
        onError: (Exception) -> Unit
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.previewView = previewView

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Set up preview use case
                preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView
                }

                // Set up image capture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Create face detection analyzer
                faceDetectionAnalyzer = FaceDetectionAnalyzer { faceDetected, faceBounds ->
                    if (!isFaceDetectionPaused) {
                        _isFaceDetected.value = faceDetected
                        currentFaceBounds = faceBounds
                    }
                }

                // Set up image analysis for face detection
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, faceDetectionAnalyzer!!)
                    }

                // Use front camera
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                // Unbind previous use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )

            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Temporarily pause face detection
     * This is useful during image capture to prevent state changes
     */
    fun pauseFaceDetection() {
        isFaceDetectionPaused = true
        Log.d(TAG, "Face detection paused")
    }

    /**
     * Resume face detection after it was paused
     */
    fun resumeFaceDetection() {
        isFaceDetectionPaused = false
        Log.d(TAG, "Face detection resumed")
    }

    /**
     * Capture an image and return it as a Bitmap
     */
    fun captureImage(
        onImageCaptured: (Bitmap) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError(IllegalStateException("Camera not initialized"))
            return
        }

        // Capture the image
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        // Convert image to bitmap
                        val fullBitmap = ImageUtils.imageProxyToBitmap(image)

                        // Verify face is still present at capture time
                        verifyFaceAtCaptureTime(fullBitmap) { isFacePresent, finalFaceBounds ->
                            if (isFacePresent) {
                                // Face is present, crop the image if we have bounds
                                val bounds = finalFaceBounds ?: currentFaceBounds

                                if (bounds != null) {
                                    // Crop to face bounds
                                    val croppedBitmap = cropToFaceBounds(fullBitmap, bounds)
                                    onImageCaptured(croppedBitmap)
                                } else {
                                    // No bounds, return full image with horizontal flip
                                    val matrix = Matrix().apply { preScale(-1f, 1f) }
                                    val flippedBitmap = Bitmap.createBitmap(
                                        fullBitmap, 0, 0, fullBitmap.width, fullBitmap.height,
                                        matrix, true
                                    )
                                    onImageCaptured(flippedBitmap)
                                }
                            } else {
                                // No face detected at capture time
                                onError(Exception("No face detected at capture time"))
                            }
                        }
                    } catch (e: Exception) {
                        onError(e)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    /**
     * Verifies if a face is present in the captured bitmap
     */
    private fun verifyFaceAtCaptureTime(
        bitmap: Bitmap,
        callback: (Boolean, Rect?) -> Unit
    ) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                val faceDetected = faces.isNotEmpty()

                // Get new face bounds from verification
                val faceBounds = if (faceDetected && faces.isNotEmpty()) {
                    val face = faces[0]
                    val bounds = face.boundingBox

                    // Add padding around the face
                    val padding = (bounds.width() * 0.2).toInt()
                    Rect(
                        bounds.left - padding,
                        bounds.top - padding,
                        bounds.right + padding,
                        bounds.bottom + padding
                    )
                } else {
                    null
                }

                callback(faceDetected, faceBounds)
            }
            .addOnFailureListener {
                // If verification fails, use the last known face bounds
                callback(false, null)
            }
    }

    /**
     * Crops a bitmap to include only the face area
     */
    private fun cropToFaceBounds(bitmap: Bitmap, bounds: Rect): Bitmap {
        // Ensure bounds are within bitmap dimensions
        val validLeft = bounds.left.coerceIn(0, bitmap.width - 1)
        val validTop = bounds.top.coerceIn(0, bitmap.height - 1)
        val validRight = bounds.right.coerceIn(validLeft + 1, bitmap.width)
        val validBottom = bounds.bottom.coerceIn(validTop + 1, bitmap.height)

        // Create cropped bitmap
        val width = validRight - validLeft
        val height = validBottom - validTop

        // Apply horizontal flip for front camera mirroring
        val matrix = Matrix().apply { preScale(-1f, 1f) }

        return try {
            // Create cropped bitmap with horizontal flip
            Bitmap.createBitmap(
                bitmap,
                validLeft,
                validTop,
                width,
                height,
                matrix,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to crop bitmap: ${e.message}")
            // Return original bitmap with horizontal flip if cropping fails
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
        }
    }

    /**
     * Release camera resources
     */
    fun shutdown() {
        try {
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down camera executor", e)
        }
    }
}