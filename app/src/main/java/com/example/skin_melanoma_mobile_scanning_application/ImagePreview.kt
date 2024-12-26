package com.example.skin_melanoma_mobile_scanning_application

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun ImagePreview(
    imageUri: Uri,
    onConfirm: () -> Unit,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onRetake,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Retake")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Confirm")
            }
        }
    }
}