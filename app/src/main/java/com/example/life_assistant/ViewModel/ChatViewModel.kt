package com.example.life_assistant.ViewModel

//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.life_assistant.model.ChatGptRequest
//import com.example.life_assistant.model.ChatGptResponse
//import com.example.life_assistant.model.Message
//import com.example.life_assistant.network.RetrofitClient
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class ChatViewModel : ViewModel() {
//    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
//    val chatMessages: StateFlow<List<Message>> get() = _chatMessages
//
//    private fun convertChineseNumberToArabic(chinese: String): Int {
//        val chineseNumbers = mapOf(
//            "零" to 0, "一" to 1, "二" to 2, "兩" to 2, "三" to 3,
//            "四" to 4, "五" to 5, "六" to 6, "七" to 7, "八" to 8, "九" to 9
//        )
//        val unitMap = mapOf("十" to 10, "百" to 100, "千" to 1000)
//
//        var total = 0
//        var current = 0
//        var multiplier = 1
//
//        for (char in chinese.reversed()) {
//            when {
//                char.toString() in chineseNumbers -> {
//                    current = chineseNumbers[char.toString()]!!
//                    total += current * multiplier
//                    current = 0
//                }
//                char.toString() in unitMap -> {
//                    multiplier = unitMap[char.toString()]!!
//                    if (current == 0) total += multiplier
//                    else {
//                        total += current * multiplier
//                        current = 0
//                    }
//                    multiplier = 1
//                }
//            }
//        }
//        return if (current != 0) total + current else total
//    }
//
//
//    // 正則表達式，抓取名稱和時間
//    private val taskPattern = """(?:安排|做|我要|幫我|需要|幫我安排)?(\S+)(?:的時間|時間)?(?:花費|估計|需要|用|大概要|大概|要)?(\S+)(小時|分鐘)""".toRegex()
//
//
//
//    fun sendMessage(userMessage: String) {
//        val currentMessages = _chatMessages.value.toMutableList()
//        currentMessages.add(Message(role = "user", content = userMessage))
//        _chatMessages.value = currentMessages
//
////        // 嘗試解析
////        val task = parseTask(userMessage)
////        task?.let {
////            val customResponse = "名稱：${it.name}\n時間：${it.duration}"
////            val updatedMessages = _chatMessages.value.toMutableList()
////            updatedMessages.add(Message(role = "assistant", content = customResponse))
////            _chatMessages.value = updatedMessages
////        } ?: run {
////            val updatedMessages = _chatMessages.value.toMutableList()
////            updatedMessages.add(Message(role = "assistant", content = "很抱歉，請再說明一次"))
////            _chatMessages.value = updatedMessages
////        }
////
////        // 如果沒有成功解析，則不發送請求給 ChatGPT API
////        if (task == null) return
//
//        // 這裡是原來的 API 請求，若成功解析則不再發送
//        val request = ChatGptRequest(messages = currentMessages)
//        Log.d("ChatViewModel", "Sending request: $request")
//
//        RetrofitClient.instance.sendMessage(request).enqueue(object : Callback<ChatGptResponse> {
//            override fun onResponse(
//                call: Call<ChatGptResponse>,
//                response: Response<ChatGptResponse>
//            ) {
//                Log.d("ChatViewModel", "Response received: $response")
//                if (response.isSuccessful) {
//                    val chatGptReply = response.body()?.choices?.firstOrNull()?.message?.content ?: "No response"
//                    Log.d("ChatViewModel", "ChatGPT reply: $chatGptReply")
//                    val updatedMessages = _chatMessages.value.toMutableList()
//                    updatedMessages.add(Message(role = "assistant", content = chatGptReply))
//                    _chatMessages.value = updatedMessages
//                } else {
//                    Log.e("ChatViewModel", "Response not successful: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ChatGptResponse>, t: Throwable) {
//                Log.e("ChatViewModel", "Request failed: ${t.message}", t)
//                val updatedMessages = _chatMessages.value.toMutableList()
//                updatedMessages.add(Message(role = "assistant", content = "Error: ${t.message}"))
//                _chatMessages.value = updatedMessages
//            }
//        })
//    }
//
//    // 用來解析使用者的訊息，提取關鍵字
//    private fun parseTask(message: String): Task? {
//        val matchResult = taskPattern.find(message)
//        return if (matchResult != null) {
//            val name = matchResult.groupValues[1]
//            var durationText = matchResult.groupValues[2]
//            val unit = matchResult.groupValues[3]
//
//            // 將國字數字轉換為阿拉伯數字
//            val durationNumber = try {
//                if (durationText.any { it in "零一二兩三四五六七八九十百千" }) {
//                    convertChineseNumberToArabic(durationText)
//                } else {
//                    durationText.toInt()
//                }
//            } catch (e: Exception) {
//                Log.e("ChatViewModel", "數字轉換失敗: $e")
//                return null
//            }
//
//            val formattedDuration = when (unit) {
//                "分鐘" -> "${durationNumber} 分鐘"
//                "小時" -> "${durationNumber} 小時"
//                else -> ""
//            }
//
//            Log.d("ChatViewModel", "解析結果: 名稱=$name, 時間=$formattedDuration")
//            Task(name, formattedDuration)
//        } else {
//            null
//        }
//    }
//
//
//}
//
//
//data class Task(val name: String, val duration: String)


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life_assistant.model.ChatGptRequest
import com.example.life_assistant.model.ChatGptResponse
import com.example.life_assistant.model.Message
import com.example.life_assistant.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> get() = _chatMessages

    fun sendMessage(userMessage: String) {
        val currentMessages = _chatMessages.value.toMutableList()
        currentMessages.add(Message(role = "user", content = userMessage))
        _chatMessages.value = currentMessages

        // 新增 Prompt
        val prompt = """
            你是一個專門幫助使用者提取行程資訊的助手，請根據以下規則回應：
            1. 如果使用者描述的任務缺少完成期限（`deadline`），請在回應中提醒使用者提供該任務的完成期限。
            2. 如果使用者的訊息不是與行程相關的問題，請回應：「此聊天室僅專注於協助使用者安排行程，請輸入與行程相關的資訊。」
            3. 請按照以下 JSON 格式輸出行程資訊：
            [
              {"task": "任務名稱", "duration": "時長（如 3小時）", "deadline": "完成期限（如 11/30）"}
            ]

            以下是使用者的輸入：
            $userMessage
        """.trimIndent()

        val request = ChatGptRequest(
            messages = listOf(
                Message(role = "system", content = "你是一個專注於解析行程資訊的助手。"),
                Message(role = "user", content = prompt)
            )
        )
        Log.d("ChatViewModel", "Sending request: $request")

        RetrofitClient.instance.sendMessage(request).enqueue(object : Callback<ChatGptResponse> {
            override fun onResponse(
                call: Call<ChatGptResponse>,
                response: Response<ChatGptResponse>
            ) {
                Log.d("ChatViewModel", "Response received: $response")
                if (response.isSuccessful) {
                    val chatGptReply = response.body()?.choices?.firstOrNull()?.message?.content ?: "無回應"
                    Log.d("ChatViewModel", "ChatGPT reply: $chatGptReply")
                    val updatedMessages = _chatMessages.value.toMutableList()
                    updatedMessages.add(Message(role = "assistant", content = chatGptReply))
                    _chatMessages.value = updatedMessages
                } else {
                    Log.e("ChatViewModel", "Response not successful: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ChatGptResponse>, t: Throwable) {
                Log.e("ChatViewModel", "Request failed: ${t.message}", t)
                val updatedMessages = _chatMessages.value.toMutableList()
                updatedMessages.add(Message(role = "assistant", content = "錯誤: ${t.message}"))
                _chatMessages.value = updatedMessages
            }
        })
    }
}