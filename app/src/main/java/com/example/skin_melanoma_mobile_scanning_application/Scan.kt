package com.example.skin_melanoma_mobile_scanning_application

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavController) {
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Handle permission granted
            showImageSourceDialog = true
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_LONG).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Process image and classify
            processAndClassifyImage(uri, context, auth, db, navController)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let {
                // Process image and classify
                processAndClassifyImage(it, context, auth, db, navController)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.Star, contentDescription = "Camera")
                Spacer(Modifier.width(8.dp))
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.Star, contentDescription = "Gallery")
                Spacer(Modifier.width(8.dp))
                Text("Choose from Gallery")
            }
        }
    }
}

private fun processAndClassifyImage(
    uri: Uri,
    context: Context,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navController: NavController
) {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

    // Initialize classifier
    val classifier = MelanomaClassifier(context)

    try {
        // Classify image
        val (isMalignant, confidence) = classifier.classifyImage(bitmap)

        // Create scan result
        val scanResult = ScanResult(
            id = UUID.randomUUID().toString(),
            userId = auth.currentUser?.uid ?: "",
            imageUrl = uri.toString(),
            diagnosis = if (isMalignant) "Malignant" else "Benign",
            confidence = confidence,
            timestamp = Date(),
            isMalignant = isMalignant
        )

        // Save to Firestore
        db.collection("scans")
            .document(scanResult.id)
            .set(scanResult)
            .addOnSuccessListener {
                // Navigate to results screen
                navController.navigate("scan_result/${scanResult.id}")
                Toast.makeText(context, "Scan completed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving scan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    } catch (e: Exception) {
        Toast.makeText(context, "Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        classifier.close()
    }
}