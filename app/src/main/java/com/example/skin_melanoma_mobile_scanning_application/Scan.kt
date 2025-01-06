package com.example.skin_melanoma_mobile_scanning_application

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavController,
    imageUri: Uri,
    classification: Pair<Boolean, Float>
) {
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    DisposableEffect(Unit) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val scanId = UUID.randomUUID().toString()

                val scanResult = ScanResult(
                    id = scanId,
                    userId = userId,
                    imageUrl = imageUri.toString(),
                    diagnosis = if (classification.first) "Malignant" else "Benign",
                    confidence = classification.second,
                    timestamp = Date(),
                    isMalignant = classification.first
                )

                db.collection("scans")
                    .document(scanId)
                    .set(scanResult)
                    .await()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error = "Error saving scan: ${e.message}"
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        }

        onDispose {
            job.cancel()
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
