package com.codelytical.facelytical

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ThirdPartyApp()
                }
            }
        }
    }
}

@Composable
fun ThirdPartyApp() {
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var showFaceCapture by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showFaceCapture) {
        // Show Face Capture Screen
        FaceCaptureLibrary.FaceCaptureView(
            onImageCaptured = { bitmap ->
                // Handle captured image
                capturedImage = bitmap
                showFaceCapture = false

                // For debug purposes
                println("Image captured: ${bitmap.width}x${bitmap.height}")
            },
            onCaptureError = { exception ->
                // Handle error
                Toast.makeText(
                    context,
                    "Capture error: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                showFaceCapture = false
                println("Capture error: $exception")
            },
            onPermissionDenied = {
                // Handle permission denied
                Toast.makeText(
                    context,
                    "Camera permission is required",
                    Toast.LENGTH_LONG
                ).show()
                showFaceCapture = false
                println("Permission denied")
            },
            onBackPressed = {
                // Handle back press
                showFaceCapture = false
                println("Back pressed")
            }
        )
    } else {
        // Show Main App UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Third Party App",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (capturedImage != null) {
                // Display captured image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Captured Face Image",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = capturedImage!!.asImageBitmap(),
                                contentDescription = "Captured Face",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Add a button to capture again
                        Button(
                            onClick = { showFaceCapture = true },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("Capture New Image")
                        }
                    }
                }
            } else {
                // Show capture button if no image is captured yet
                Button(
                    onClick = { showFaceCapture = true },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Capture Face Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tap the button to start face capture",
                    color = Color.Gray
                )
            }
        }
    }
}