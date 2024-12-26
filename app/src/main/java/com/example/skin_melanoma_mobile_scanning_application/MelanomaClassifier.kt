package com.example.skin_melanoma_mobile_scanning_application

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MelanomaClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "melanoma_classifier.tflite" // Replace with your model filename
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

            // Log model information
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            Log.d("MelanomaClassifier", "Model Input Shape: ${inputShape?.contentToString()}")
            Log.d("MelanomaClassifier", "Model Output Shape: ${outputShape?.contentToString()}")

            Log.d("MelanomaClassifier", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("MelanomaClassifier", "Error setting up interpreter: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        try {
            val assetFileDescriptor = context.assets.openFd(modelPath)
            Log.d("MelanomaClassifier", "Model file found, size: ${assetFileDescriptor.length}")
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e("MelanomaClassifier", "Error loading model file: ${e.message}")
            throw e
        }
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
        if (interpreter == null) {
            Log.e("MelanomaClassifier", "Interpreter is null")
            return Pair(false, 0.0f)
        }

        try {
            Log.d("MelanomaClassifier", "Starting image classification")
            val inputBuffer = preprocessBitmap(bitmap)
            val outputArray = Array(1) { FloatArray(1) } // Single output value

            // Get output tensor info
            val outputTensor = interpreter?.getOutputTensor(0)
            Log.d("MelanomaClassifier", "Output Tensor Shape: ${outputTensor?.shape()?.contentToString()}")
            Log.d("MelanomaClassifier", "Output Tensor Type: ${outputTensor?.dataType()}")

            try {
                interpreter?.run(inputBuffer, outputArray)
                val prediction = outputArray[0][0]
                Log.d("MelanomaClassifier", "Raw prediction value: $prediction")

                val isMalignant = prediction > 0.5f
                val confidence = if (isMalignant) prediction else (1 - prediction)

                return Pair(isMalignant, confidence)
            } catch (e: Exception) {
                Log.e("MelanomaClassifier", "Error during model inference: ${e.message}")
                throw e
            }
        } catch (e: Exception) {
            Log.e("MelanomaClassifier", "Error classifying image: ${e.message}")
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