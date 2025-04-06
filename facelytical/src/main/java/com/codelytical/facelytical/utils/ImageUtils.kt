package com.codelytical.facelytical.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import kotlin.experimental.and
import android.util.Base64
import androidx.core.location.LocationRequestCompat.Quality

/**
 * Utility functions for image processing
 */
object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Converts an ImageProxy to a Bitmap
     */
    @OptIn(ExperimentalGetImage::class)
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val image = imageProxy.image ?: throw IllegalArgumentException("Image proxy has null image")

        // Try the direct conversion first - safest approach
        return try {
            imageToBitmap(image, imageProxy.imageInfo.rotationDegrees)
        } catch (e: Exception) {
            Log.e(TAG, "Primary conversion failed: ${e.message}", e)
            // Fallback to alternative method if the first one fails
            fallbackImageProxyToBitmap(imageProxy)
        }
    }

    /**
     * Primary method to convert Image to Bitmap
     */
    private fun imageToBitmap(image: Image, rotation: Int): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return correctBitmapOrientation(bitmap, rotation)
    }

    /**
     * Fallback method if primary conversion fails
     */
    @OptIn(ExperimentalGetImage::class)
    private fun fallbackImageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val image = imageProxy.image ?: throw IllegalArgumentException("Image proxy has null image")
        val format = image.format

        // Handle YUV_420_888 format
        if (format == ImageFormat.YUV_420_888) {
            val width = image.width
            val height = image.height

            // Get the YUV planes
            val planes = image.planes

            // Ensure we have valid planes before proceeding
            if (planes.isEmpty()) {
                throw IllegalArgumentException("Image has no planes")
            }

            val yPlane = planes[0]
            val yBuffer = yPlane.buffer
            val ySize = yBuffer.remaining()
            val yuvBytes = ByteArray(ySize)
            yBuffer.get(yuvBytes, 0, ySize)

            // Create a grayscale bitmap from Y plane if other planes aren't available
            if (planes.size == 1) {
                val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val value = yuvBytes[y * yPlane.rowStride + x] and 0xFF.toByte()
                        val pixel = 0xFF000000.toInt() or (value.toInt() shl 16) or (value.toInt() shl 8) or value.toInt()
                        grayscaleBitmap.setPixel(x, y, pixel)
                    }
                }
                return correctBitmapOrientation(grayscaleBitmap, imageProxy.imageInfo.rotationDegrees)
            }

            // Use YuvImage for conversion if all planes are available
            val uPlane = planes[1]
            val vPlane = planes[2]

            val uBuffer = uPlane.buffer
            val vBuffer = vPlane.buffer

            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            // U and V are swapped
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            return correctBitmapOrientation(bitmap, imageProxy.imageInfo.rotationDegrees)
        } else {
            // For other formats, try a general approach
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            return correctBitmapOrientation(bitmap, imageProxy.imageInfo.rotationDegrees)
        }
    }

    /**
     * Corrects the orientation of a bitmap based on rotation degrees
     *
     * This function ensures proper orientation for front camera images
     */
    private fun correctBitmapOrientation(bitmap: Bitmap, rotation: Int): Bitmap {
        val matrix = Matrix()

        // Front camera adjustment (mirror horizontally)
        // Note: The horizontal flip (-1 on x-axis) will be applied in CameraManager
        // when creating the final bitmap to avoid double flipping

        when (rotation) {
            0 -> {
                // No rotation needed
            }
            90 -> {
                // Portrait orientation - rotate properly for front camera
                matrix.postRotate(90f)
            }
            180 -> {
                // Upside down
                matrix.postRotate(180f)
            }
            270 -> {
                // Portrait orientation, phone upside down
                matrix.postRotate(270f)
            }
            else -> {
                // Handle any other rotation
                matrix.postRotate(rotation.toFloat())
            }
        }

        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    fun bitmapToBase64(bitmap: Bitmap?, quality: Int = 100): String? {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null. Cannot convert to base64.")
            return null
        }

        return try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)) {
                    Log.e(TAG, "Failed to compress Bitmap to base64.")
                    return null
                }
                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert Bitmap to base64: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}