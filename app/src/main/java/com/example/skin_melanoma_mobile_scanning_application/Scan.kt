package com.example.skin_melanoma_mobile_scanning_application

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

data class ScanResultData(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val imageUrl: String = "",
    val diagnosis: String = "",
    val confidence: Float = 0f,
    val timestamp: Date = Date(),
    val isMalignant: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavController,
    imageUri: Uri,
    classification: Pair<Boolean, Float>
) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Call the non-composable function
                uploadAndSaveScan(context, imageUri, classification) { success, message ->
                    isLoading = false
                    if (!success) {
                        error = message
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                error = e.message
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Scanned image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (classification.first) "Malignant" else "Benign",
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (classification.first) Color.Red else Color.Green
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Confidence: ${(classification.second * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Scan History")
                    }
                }
            }
        }
    }
}

// Separate non-composable function for Firebase operations
private suspend fun uploadAndSaveScan(
    context: Context,
    imageUri: Uri,
    classification: Pair<Boolean, Float>,
    callback: (Boolean, String?) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val userId = auth.currentUser?.uid ?: run {
        callback(false, "User not logged in")
        return
    }

    try {
        val scanId = UUID.randomUUID().toString()

        // Create scan result document without image URL
        val scanResult = ScanResultData(
            id = scanId,
            userId = userId,
            imageUrl = "", // Empty for now since we're not using Storage
            diagnosis = if (classification.first) "Malignant" else "Benign",
            confidence = classification.second,
            timestamp = Date(),
            isMalignant = classification.first
        )

        // Save to Firestore
        db.collection("scans")
            .document(scanId)
            .set(scanResult)
            .await()

        callback(true, null)
    } catch (e: Exception) {
        callback(false, "Failed to save scan result: ${e.message}")
    }
}