package com.example.skin_melanoma_mobile_scanning_application

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MelanomaClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "app/src/main/assets/melanoma_classifier.tflite" // Replace with your model filename
    private val imageSize = 224 // Change according to your model's input size
    private val PIXEL_SIZE = 3
    private val BATCH_SIZE = 1
    private val CHANNEL_SIZE = 3 // RGB
    private val IMAGE_MEAN = 127.5f
    private val IMAGE_STD = 127.5f

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val tfliteModel = loadModelFile()
            val options = Interpreter.Options()
            interpreter = Interpreter(tfliteModel, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)

        val inputBuffer = ByteBuffer.allocateDirect(
            BATCH_SIZE * imageSize * imageSize * CHANNEL_SIZE * 4
        ).apply {
            order(ByteOrder.nativeOrder())
        }

        val pixels = IntArray(imageSize * imageSize)
        scaledBitmap.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Normalize pixel values
            inputBuffer.putFloat((r - IMAGE_MEAN) / IMAGE_STD)
            inputBuffer.putFloat((g - IMAGE_MEAN) / IMAGE_STD)
            inputBuffer.putFloat((b - IMAGE_MEAN) / IMAGE_STD)
        }

        scaledBitmap.recycle()
        return inputBuffer
    }

    fun classifyImage(bitmap: Bitmap): Pair<Boolean, Float> {
        // Preprocess the image
        val inputBuffer = preprocessBitmap(bitmap)

        // Create output array (adjust size based on your model's output)
        val outputBuffer = ByteBuffer.allocateDirect(4 * 2).apply { // 2 classes
            order(ByteOrder.nativeOrder())
        }
        val outputArray = Array(1) { FloatArray(2) }

        try {
            // Run inference
            interpreter?.run(inputBuffer, outputArray)

            // Process results
            val isMalignant = outputArray[0][1] > outputArray[0][0]
            val confidence = if (isMalignant) outputArray[0][1] else outputArray[0][0]

            return Pair(isMalignant, confidence)
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false, 0.0f)
        }
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}