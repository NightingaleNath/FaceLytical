# FaceLytical

A modern Jetpack Compose library for face capture and detection in Android applications.

## Features

- Real-time face detection
- Clean, modern UI with customizable elements
- User-friendly face capture instructions
- Comprehensive error handling
- Built with Jetpack Compose for smooth integration with modern Android apps

## Installation

### 1. Add JitPack repository

Add JitPack to your project's build.gradle file:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Or in settings.gradle:

```groovy
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add the dependency

Add the FaceLytical dependency to your app's build.gradle file:

```groovy
dependencies {
    implementation "com.github.NightingaleNath:FaceLytical:1.0.0"
    
    // Required dependencies
    implementation "androidx.core:core-ktx:1.15.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7"
    implementation "androidx.activity:activity-compose"
    implementation platform("androidx.compose:compose-bom:2025.03.01")
    implementation "androidx.compose.ui:ui"
    implementation "androidx.compose.ui:ui-graphics"
    implementation "androidx.compose.ui:ui-tooling-preview"
    implementation "com.google.android.material:material:1.12.0"
    implementation "androidx.compose.material3:material3"
}
```

## Usage

### Basic Implementation

FaceLytical is designed to work seamlessly with Jetpack Compose. Here's a basic implementation:

```kotlin
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelytical.facelytical.FaceCaptureLibrary
import com.codelytical.facelytical.utils.ImageUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FaceCaptureDemo()
                }
            }
        }
    }
}

@Composable
fun FaceCaptureDemo() {
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
                
                // Convert bitmap to base64 if needed
                val base64Image = ImageUtils.bitmapToBase64(bitmap)
                println("Base64 image: $base64Image")
            },
            onCaptureError = { exception ->
                // Handle error
                Toast.makeText(
                    context,
                    "Capture error: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                showFaceCapture = false
            },
            onPermissionDenied = {
                // Handle permission denied
                Toast.makeText(
                    context,
                    "Camera permission is required",
                    Toast.LENGTH_LONG
                ).show()
                showFaceCapture = false
            },
            onBackPressed = {
                // Handle back press
                showFaceCapture = false
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
                text = "Face Capture Demo",
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
```

## API Reference

### FaceCaptureLibrary

The main entry point for the library:

```kotlin
FaceCaptureLibrary.FaceCaptureView(
    onImageCaptured: (Bitmap) -> Unit,
    onCaptureError: (Exception) -> Unit,
    onPermissionDenied: () -> Unit,
    onBackPressed: () -> Unit = {}
)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `onImageCaptured` | `(Bitmap) -> Unit` | Called when an image is successfully captured. Provides the bitmap of the captured face. |
| `onCaptureError` | `(Exception) -> Unit` | Called when an error occurs during capture. Provides the exception with details. |
| `onPermissionDenied` | `() -> Unit` | Called when camera permission is denied by the user. |
| `onBackPressed` | `() -> Unit` | Called when the user presses the back button or the close button. Optional parameter. |

### ImageUtils

Utility class for image operations:

```kotlin
// Convert bitmap to base64 string
val base64String = ImageUtils.bitmapToBase64(bitmap)

// Convert base64 string to bitmap (if needed)
val bitmap = ImageUtils.base64ToBitmap(base64String)
```

## Permissions

The library requires camera permission. Add this to your AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

The library handles permission requests internally, but you should implement the `onPermissionDenied` callback to handle cases where the user denies camera access.

## Customization

The library uses a modern UI design with sensible defaults, but you can customize the appearance by implementing your own theme.

## Error Handling

The library provides comprehensive error handling through the `onCaptureError` callback. Common errors include:

- Camera initialization failures
- Permission issues
- Face detection failures
- Image processing errors

Handle these errors appropriately in your implementation.

## License

[Add your license information here]

## Credits

Developed by [CodeLytical Hub]
