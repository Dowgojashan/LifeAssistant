package com.example.life_assistant

import java.nio.ByteBuffer

interface TFLiteModelInterface {
    fun runInference(input: ByteBuffer): Array<FloatArray>
    fun close()
}
