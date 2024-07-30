package com.example.newmodeltest

import java.nio.ByteBuffer

interface TFLiteModelInterface {
    fun runInference(input: ByteBuffer): Array<FloatArray>
    fun close()
}
