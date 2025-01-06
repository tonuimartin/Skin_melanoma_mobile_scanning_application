package com.example.skin_melanoma_mobile_scanning_application

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MelanomaClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "melanoma_classifier.tflite"
    private var inputImageWidth = 0
    private var inputImageHeight = 0
    private var inputImageChannels = 3

    companion object {
        const val MODEL_VERSION = "1.0"
        private const val TAG = "MelanomaClassifier"
    }

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val tfliteModel = loadModelFile()
            val options = Interpreter.Options()
            interpreter = Interpreter(tfliteModel, options)

            // Get input tensor shape
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            inputImageHeight = inputShape?.get(1) ?: 224
            inputImageWidth = inputShape?.get(2) ?: 224
            inputImageChannels = inputShape?.get(3) ?: 3

            Log.d(TAG, "Model Input Shape: ${inputShape?.contentToString()}")
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up interpreter: ${e.message}")
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
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val byteBuffer = ByteBuffer.allocateDirect(inputImageWidth * inputImageHeight * inputImageChannels * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputImageWidth * inputImageHeight)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f)
            byteBuffer.putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f)
        }

        return byteBuffer
    }

    fun classifyImage(bitmap: Bitmap): Pair<Boolean, Float> {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter is null")
            return Pair(false, 0.0f)
        }

        try {
            val inputBuffer = preprocessBitmap(bitmap)
            val outputBuffer = ByteBuffer.allocateDirect(4)
            outputBuffer.order(ByteOrder.nativeOrder())

            interpreter?.run(inputBuffer, outputBuffer)

            outputBuffer.rewind()
            val prediction = outputBuffer.float

            Log.d(TAG, "Raw prediction value: $prediction")

            val isMalignant = prediction > 0.5f
            val confidence = if (isMalignant) prediction else (1 - prediction)

            return Pair(isMalignant, confidence)
        } catch (e: Exception) {
            Log.e(TAG, "Error classifying image: ${e.message}")
            e.printStackTrace()
            return Pair(false, 0.0f)
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}