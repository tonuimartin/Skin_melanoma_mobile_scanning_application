package com.example.skin_melanoma_mobile_scanning_application

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(navController: NavController) {
    var scans by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val deleteJob = remember { mutableStateOf<Job?>(null) }

    // Function to fetch scans
    val fetchScans = suspend {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val querySnapshot = db.collection("scans")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(ScanResult::class.java)
            }
        } else {
            emptyList()
        }
    }

    val deleteScan = remember {
        { scan: ScanResult ->
            deleteJob.value?.cancel()
            deleteJob.value = CoroutineScope(Dispatchers.IO).launch {
                try {

                    withContext(Dispatchers.Main) {
                        scans = scans.filter { it.id != scan.id }
                    }

                    db.collection("scans")
                        .document(scan.id)
                        .delete()
                        .await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Scan deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        scans = fetchScans()
                        Toast.makeText(context, "Failed to delete scan: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            deleteJob.value?.cancel()
        }
    }

    LaunchedEffect(Unit) {
        try {
            scans = fetchScans()
            isLoading = false
        } catch (e: Exception) {
            error = "Error loading scan history: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
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
            } else if (scans.isEmpty() && error == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No scan history found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("scan") }
                    ) {
                        Text("Start a New Scan")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = scans,
                        key = { it.id }
                    ) { scan ->
                        ScanHistoryItem(
                            scan = scan,
                            onConfirmDelete = { deleteScan(scan) }
                        )
                    }
                }
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScanHistoryItem(
    scan: ScanResult,
    onConfirmDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(scan.timestamp),
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete scan")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scan.diagnosis,
                style = MaterialTheme.typography.titleMedium,
                color = if (scan.isMalignant) Color.Red else Color.Green
            )

            Text(
                text = "Confidence: ${(scan.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Scan") },
            text = { Text("Are you sure you want to delete this scan?") },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}