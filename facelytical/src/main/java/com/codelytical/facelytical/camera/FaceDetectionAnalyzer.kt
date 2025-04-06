package com.codelytical.facelytical.camera

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Image analyzer that detects faces in camera frames
 */
class FaceDetectionAnalyzer(
    private val onFaceDetected: (Boolean, Rect?) -> Unit
) : ImageAnalysis.Analyzer {
    private val TAG = "FaceDetectionAnalyzer"
    private val isProcessing = AtomicBoolean(false)

    // Configure face detector options for performance
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.15f)
        .build()

    private val faceDetector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        // Skip processing if we're already analyzing an image
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }

        isProcessing.set(true)

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            isProcessing.set(false)
            return
        }

        // Prepare the input image for ML Kit
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        // Process the image and detect faces
        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                val faceDetected = faces.isNotEmpty()

                // Extract face bounds if a face is detected
                val faceBounds = if (faceDetected && faces.isNotEmpty()) {
                    // Add padding to the face bounds for better framing
                    val face = faces[0]
                    val bounds = face.boundingBox

                    // Add 20% padding around the face
                    val widthPadding = (bounds.width() * 0.2).toInt()
                    val heightPadding = (bounds.height() * 0.2).toInt()

                    Rect(
                        bounds.left - widthPadding,
                        bounds.top - heightPadding,
                        bounds.right + widthPadding,
                        bounds.bottom + heightPadding
                    )
                } else {
                    null
                }

                // Pass face detection status and bounds
                onFaceDetected(faceDetected, faceBounds)
                isProcessing.set(false)
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed", e)
                onFaceDetected(false, null)
                isProcessing.set(false)
                imageProxy.close()
            }
    }
}