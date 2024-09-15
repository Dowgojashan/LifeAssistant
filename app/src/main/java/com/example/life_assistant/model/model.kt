package com.example.life_assistant

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.life_assistant.ui.theme.Life_AssistantTheme
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

val predefinedSchedules = mapOf(
//        # 0: 工作, 1: 娛樂, 2: 運動, 3: 生活雜務, 4: 讀書, 5: 旅遊, 6: 吃飯
    "做報告" to "工作",
    "開會" to "工作",
    "去東京出差" to "工作",
    "打工" to "工作",
    "看YT" to "娛樂",
    "看電影" to "娛樂",
    "玩手機" to "娛樂",
    "打電腦" to "娛樂",
    "英雄聯盟" to "娛樂",
    "玩荒野亂鬥" to "娛樂",
    "傳說對決" to "娛樂",
    "貓咪大戰爭" to "娛樂",
    "看書" to "娛樂",
    "聽音樂" to "娛樂",
    "看影集" to "娛樂",
    "看黑袍糾察隊" to "娛樂",
    "看瑞克與莫蒂" to "娛樂",
    "看NBA" to "娛樂",
    "小巨蛋溜冰" to "運動",
    "去健身房" to "運動",
    "打球" to "運動",
    "打籃球" to "運動",
    "打網球" to "運動",
    "打棒球" to "運動",
    "打桌球" to "運動",
    "打羽球" to "運動",
    "游泳" to "運動",
    "浮潛" to "運動",
    "比賽" to "運動",
    "慢跑" to "運動",
    "晨跑" to "運動",
    "洗碗" to "生活雜務",
    "掃地" to "生活雜務",
    "打掃" to "生活雜務",
    "大掃除" to "生活雜務",
    "摺衣服" to "生活雜務",
    "收房間" to "生活雜務",
    "丟垃圾" to "生活雜務",
    "倒垃圾" to "生活雜務",
    "洗衣服" to "生活雜務",
    "洗床單" to "生活雜務",
    "接女友" to "生活雜務",
    "接小孩" to "生活雜務",
    "家長會" to "生活雜務",
    "印文件" to "生活雜務",
    "做專題" to "讀書",
    "讀資結" to "讀書",
    "讀物理" to "讀書",
    "讀化學" to "讀書",
    "寫數學" to "讀書",
    "算數學" to "讀書",
    "做化學實驗" to "讀書",
    "寫題目" to "讀書",
    "寫考古題" to "讀書",
    "讀計概" to "讀書",
    "讀資訊系統" to "讀書",
    "背英文單字" to "讀書",
    "寫英文作文" to "讀書",
    "讀英文文章" to "讀書",
    "寫作文" to "讀書",
    "寫申論題" to "讀書",
    "讀論文" to "讀書",
    "寫作業" to "讀書",
    "去逛街" to "旅遊",
    "去西門町" to "旅遊",
    "去台北車站" to "旅遊",
    "去北車" to "旅遊",
    "去地下街" to "旅遊",
    "去中原夜市" to "旅遊",
    "去東京玩" to "旅遊",
    "去日本" to "旅遊",
    "去大阪" to "旅遊",
    "去清水寺" to "旅遊",
    "去逛錦市場" to "旅遊",
    "逛夜市" to "旅遊",
    "參觀神社" to "旅遊",
    "吃早餐" to "吃飯",
    "吃午餐" to "吃飯",
    "吃晚餐" to "吃飯",
    "吃海底撈" to "吃飯",
    "吃烤肉" to "吃飯",
    "烤肉" to "吃飯",
    "煮火鍋" to "吃飯",
    "吃錢都" to "吃飯",
    "吃壽司" to "吃飯",
    "吃爭鮮" to "吃飯",
    "吃壽司郎" to "吃飯",
    "吃漢堡" to "吃飯",
    "爽喀爆米花" to "吃飯",
    "猛吃拉麵" to "吃飯",
    "麥當勞" to "吃飯",
    "肯德基" to "吃飯",
    "摩斯" to "吃飯",
    "必勝客" to "吃飯",
    "拿坡里" to "吃飯",
    "炸雞" to "吃飯"
)

@Composable
fun ModelInferenceScreen(tfliteModel: TFLiteModelInterface, vocab: Map<String, Int>) {
    // 預設的行程與分類對應表


    var userInput by remember { mutableStateOf("") }
    var inferenceResult by remember { mutableStateOf("Inference result will be displayed here") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        TextField(
            value = userInput,
            onValueChange = { newText -> userInput = newText },
            label = { Text("Enter your activity") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {

            val normalizedInput = userInput.trim().replace("\\s+".toRegex(), " ")
            // 檢查使用者輸入是否在預設行程中
            val predefinedClassification = predefinedSchedules[normalizedInput]

            if (predefinedClassification != null) {
                // 如果匹配到預設行程，直接顯示分類結果
                inferenceResult = "Predicted label: $predefinedClassification"
            } else {
                // 否則，執行模型推論
                val input = tokenize(userInput, vocab)
                val inputBuffer = ByteBuffer.allocateDirect(768 * 4).order(ByteOrder.nativeOrder())
                inputBuffer.asFloatBuffer().put(input)
                try {
                    val output = tfliteModel.runInference(inputBuffer)
                    val result = output[0]

                    // 找到推論的分類標籤（去除信心水準顯示）
                    resultText = result.indices.maxByOrNull { result[it] }?.let { predictedLabel ->
                        "Predicted label: $predictedLabel"
                    } ?: "Prediction failed"

                    inferenceResult = resultText
                } catch (e: Exception) {
                    Log.e("ModelInferenceScreen", "Inference failed", e)
                    inferenceResult = "Inference failed: ${e.message}"
                }
            }
        }) {
            Text("Run Inference")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = inferenceResult)
    }
}

fun tokenize(text: String, vocab: Map<String, Int>): FloatArray {
    val tokens = text.split(" ") // 這裡假設文本已經被分詞
    return tokens.map { token -> vocab[token]?.toFloat() ?: vocab["[UNK]"]?.toFloat() ?: 0f }.toFloatArray()
}

// 加載tokenizer的詞彙表文件
fun loadVocab(context: Context, assetFileName: String): Map<String, Int> {
    val vocab = mutableMapOf<String, Int>()
    context.assets.open(assetFileName).use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String?
            var index = 0
            while (reader.readLine().also { line = it } != null) {
                vocab[line!!] = index++
            }
        }
    }
    return vocab
}
