package com.codelytical.facelytical.ui.models

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class for face capture tutorial steps
 */
data class TutorialStep(
    val title: String,
    val description: String,
    val imageVector: ImageVector
)