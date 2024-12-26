package com.example.skin_melanoma_mobile_scanning_application

import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import java.net.URLEncoder
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.nio.charset.StandardCharsets


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showSelectionDialog by remember { mutableStateOf(true) }
    var imageSource by remember { mutableStateOf<ImageSource?>(null) }
    val context = LocalContext.current
    val classifier = remember { MelanomaClassifier(context) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            showSelectionDialog = false
            imageSource = ImageSource.GALLERY
        }
    }

    if (showSelectionDialog && imageSource == null) {
        AlertDialog(
            onDismissRequest = {
                navController.navigateUp()
            },
            title = { Text("Choose Image Source") },
            text = { Text("Would you like to take a new photo or select from gallery?") },
            confirmButton = {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showCamera = true
                            showSelectionDialog = false
                            imageSource = ImageSource.CAMERA
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Camera",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo")
                    }

                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { navController.navigateUp() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCamera) {
        CameraPreview(
            onImageCaptured = { uri ->
                imageUri = uri
                showCamera = false
            },
            onError = { exception ->
                // Handle error
                Log.e("CameraPreview", "Error capturing image", exception)
            },
            onClose = {
                showCamera = false
                showSelectionDialog = true
                imageSource = null
            }
        )
    }

    imageUri?.let { uri ->
        ImagePreview(
            imageUri = uri,
            onConfirm = {
                // Load the image and classify it
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                val classification = classifier.classifyImage(bitmap)

                navController.navigate(
                    "scanResult/${URLEncoder.encode(uri.toString(), StandardCharsets.UTF_8.toString())}/${classification.first}/${classification.second}"
                )
            },
            onRetake = {
                imageUri = null
                when (imageSource) {
                    ImageSource.CAMERA -> showCamera = true
                    ImageSource.GALLERY -> imagePickerLauncher.launch("image/*")
                    null -> showSelectionDialog = true
                }
            }
        )
    }
}

private enum class ImageSource {
    CAMERA,
    GALLERY
}