package com.example.newmodeltest

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Float

class MainActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter
    private lateinit var inputEditText: EditText
    private lateinit var resultTextView: TextView
    private lateinit var classifyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputEditText = findViewById(R.id.inputEditText)
        resultTextView = findViewById(R.id.resultTextView)
        classifyButton = findViewById(R.id.classifyButton)

        // Load TensorFlow Lite model
        tflite = Interpreter(loadModelFile("model.tflite"))

        classifyButton.setOnClickListener {
            val inputText = inputEditText.text.toString().trim()
            classifyEvent(inputText)
        }
    }

    private fun loadModelFile(modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun classifyEvent(event: String) {
        // Tokenization and preprocessing
        val inputData = preprocessInput(event)

        val outputData = Array(1) { FloatArray(7) } // Assuming there are 7 classes

        // Run the model
        tflite.run(inputData, outputData)

        // Log the output probabilities for debugging
        Log.d("ModelOutput", outputData[0].joinToString(", "))

        // Find the class with the highest probability
        val predictedClass = outputData[0].indexOfMax()
        resultTextView.text = "Predicted Class: $predictedClass"
    }

    private fun preprocessInput(event: String): ByteBuffer {
        val tokenizer = BertTokenizer.from_pretrained("bert-base-chinese") // Ensure this is accessible
        val tokens = tokenizer.encode(event) // Tokenize the input string
        val inputIds = IntArray(768) // Input size should match the model's expected input

        // Fill inputIds with the token IDs (for simplicity, assuming max length is 768)
        for (i in tokens.indices) {
            inputIds[i] = tokens[i]
        }

        // Create a ByteBuffer to hold input data
        val inputBuffer = ByteBuffer.allocateDirect(4 * inputIds.size)
        for (id in inputIds) {
            inputBuffer.putFloat(id.toFloat()) // Assuming the model expects float input
        }
        inputBuffer.rewind()
        return inputBuffer
    }

    private fun FloatArray.indexOfMax(): Int {
        var maxIndex = 0
        for (i in 1 until size) {
            if (this[i] > this[maxIndex]) {
                maxIndex = i
            }
        }
        return maxIndex
    }

    override fun onDestroy() {
        super.onDestroy()
        tflite.close() // Close the interpreter to free resources
    }
}




//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.newmodeltest.ui.theme.TFliteTestTheme
//import java.io.IOException
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//
//class MainActivity : ComponentActivity() {
//    private lateinit var tfliteModel: TFLiteModelInterface
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        try {
//            tfliteModel = TFLiteModel(this, "model.tflite")
//            Log.d("ModelLoading","成功下載模型")
//        } catch (e: IOException) {
//            Log.e("ModelLoading","Failed to load model",e)
//        }
//
//        setContent {
//            TFliteTestTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    ModelInferenceScreen(tfliteModel)
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        tfliteModel.close()
//    }
//}
//
//@Composable
//fun ModelInferenceScreen(tfliteModel: TFLiteModelInterface) {
//    var inferenceResult by remember { mutableStateOf("Inference result will be displayed here") }
//    val new_texts = listOf("組織會議", "跑步", "洗碗", "讀資結", "看演唱會", "看醫生", "跟陳仲儼開會")
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
//    ) {
//        Button(onClick = {
//            val inputs = new_texts.map { tokenize(it) }
//            Log.d("ModelInferenceScreen", "Inputs: ${inputs.joinToString()}")
//            try {
//                val results = inputs.map { input ->
//                    val inputBuffer = ByteBuffer.allocateDirect(4 * 768).order(ByteOrder.nativeOrder())
//                    inputBuffer.asFloatBuffer().put(input)
//                    val output = tfliteModel.runInference(inputBuffer)
//                    // 打印原始输出概率
//                    Log.d("ModelInferenceScreen", "Raw output: ${output[0].contentToString()}")
//                    output[0]
//                }
//
//                inferenceResult = new_texts.zip(results).mapIndexed { index, (text, result) ->
//                    val predictedLabel = result.indices.maxByOrNull { result[it] } ?: -1
//                    val confidence = result[predictedLabel]
//                    "Text: \"$text\" - Predicted label: $predictedLabel (confidence: ${String.format("%.2f", confidence)})"
//                }.joinToString("\n")
//                Log.d("ModelInferenceScreen", "Results: $inferenceResult")
//            } catch (e: Exception) {
//                Log.e("ModelInferenceScreen", "Inference failed", e)
//                inferenceResult = "Inference failed: ${e.message}"
//            }
//        }) {
//            Text("Run Inference")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = inferenceResult)
//    }
//}
//
//fun tokenize(text: String): FloatArray {
//    val vocab = mapOf(
//        "組織會議" to 1, "跑步" to 2, "洗碗" to 3, "讀資結" to 4,
//        "看演唱會" to 5, "看醫生" to 6, "跟陳仲儼開會" to 7
//    )
//    val tokenIds = listOf(1.0f, 2.0f, 3.0f) // 这里需要使用 tokenizer 的 output
//    return FloatArray(768) { if (it < tokenIds.size) tokenIds[it] else 0f }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ModelInferenceScreenPreview() {
//    TFliteTestTheme {
//        ModelInferenceScreen(TFLiteModelPreview())
//    }
//}
//
//class TFLiteModelPreview : TFLiteModelInterface {
//    override fun runInference(input: ByteBuffer): Array<FloatArray> {
//        // 假設模型的輸出是形狀 [1, 7]
//        return arrayOf(floatArrayOf(0.1f, 0.2f, 0.1f, 0.2f, 0.1f, 0.2f, 0.1f))
//    }
//
//    override fun close() {}
//}
