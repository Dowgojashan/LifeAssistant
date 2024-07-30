package com.example.newmodeltest

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer

class TFLiteModel(context: Context, modelPath: String) : TFLiteModelInterface {

    private var interpreter: Interpreter

    init {
        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model)
    }

    override fun runInference(input: ByteBuffer): Array<FloatArray> {
        // 獲取輸出張量的信息
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()

        // 創建輸出緩衝區
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputTensor.dataType())

        // 運行推理
        interpreter.run(input, outputBuffer.buffer)

        // 將輸出轉換為二維 FloatArray
        return Array(outputShape[0]) { i ->
            FloatArray(outputShape[1]) { j ->
                outputBuffer.getFloatValue(i * outputShape[1] + j)
            }
        }
    }

    override fun close() {
        interpreter.close()
    }
}