package com.codelytical.facelytical.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.codelytical.facelytical.ui.models.TutorialStep

@Composable
fun FaceWalkthroughDialog(onDismiss: () -> Unit) {
    // Dialog size modifier
    val dialogModifier = Modifier
        .fillMaxWidth(0.98f)  // Use 98% of screen width
        .fillMaxHeight(0.8f)  // Use 80% of screen height
        .background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(16.dp))

    // Remember tutorial step
    var currentStep by remember { mutableIntStateOf(0) }

    // Tutorial content for face capture
    val tutorials = listOf(
        TutorialStep(
            title = "Position Your Face Properly",
            description = "Center your face within the frame and make sure your entire face is visible. Keep a neutral expression for best results.",
            imageVector = Icons.Default.Face
        ),
        TutorialStep(
            title = "Good Lighting is Essential",
            description = "Ensure your face is well lit with even lighting. Avoid harsh shadows or bright light directly behind you that can cause silhouetting.",
            imageVector = Icons.Default.WbSunny
        ),
        TutorialStep(
            title = "Remove Obstructions",
            description = "Remove sunglasses, hats, or other items that might obscure facial features. Ensure there are no objects partially blocking your face.",
            imageVector = Icons.Default.Image
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = dialogModifier, contentAlignment = Alignment.Center) {
            // Content is in a Box with fillMaxSize to allow positioning
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Main content column with proper spacing
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    // Face illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                            .border(2.dp, Color(0xFF4BB6EF), RoundedCornerShape(12.dp))
                            .background(Color.Black, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Face icon illustration
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tutorials[currentStep].imageVector,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(36.dp)
                            .background(Color.Red, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Added spacer between close button and title
                    Spacer(modifier = Modifier.height(24.dp))

                    // Tutorial title
                    Text(
                        text = tutorials[currentStep].title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Increased space between title and description
                    Spacer(modifier = Modifier.height(20.dp))

                    // Tutorial description
                    Text(
                        text = tutorials[currentStep].description,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Navigation dots positioned above the buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tutorials.forEachIndexed { index, _ ->
                            val selected = index == currentStep
                            val dotSize by animateDpAsState(
                                targetValue = if (selected) 10.dp else 6.dp,
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = LinearOutSlowInEasing
                                ),
                                label = "dotSizeAnimation"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(dotSize)
                                    .background(
                                        color = if (selected) Color(0xFF4BB6EF) else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable { currentStep = index }
                            )
                        }
                    }
                }

                // Navigation buttons at the bottom
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (currentStep > 0) currentStep-- else onDismiss()
                        },
                        enabled = true,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if(currentStep == 0) "SKIP" else "BACK",
                            color = if (currentStep > 0) Color(0xFF4BB6EF) else Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            if (currentStep < tutorials.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (currentStep < tutorials.size - 1) "NEXT" else "DONE",
                            color = Color(0xFF4BB6EF),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}