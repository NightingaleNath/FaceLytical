package com.codelytical.facelytical.utils

/**
 * Contains all instruction text messages for the face capture process
 */
object Instructions {
    // When no face is detected in the frame
    const val POSITION_FACE = "Position your face in the frame"

    // When a face is detected and ready to capture
    const val FACE_DETECTED = "Face detected, tap button to capture"

    // When capture is in progress
    const val CAPTURING = "Capturing face, please hold still"
}