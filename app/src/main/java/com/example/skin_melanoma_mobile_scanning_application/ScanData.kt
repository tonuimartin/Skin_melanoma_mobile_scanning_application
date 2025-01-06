package com.example.skin_melanoma_mobile_scanning_application

import java.util.Date

data class ScanResult(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val diagnosis: String = "",
    val confidence: Float = 0f,
    val timestamp: Date = Date(),
    val isMalignant: Boolean = false,
    val processingTimeMs: Long = 0,
    val modelVersion: String = "",
    val rawProbabilities: List<Float> = emptyList()
)