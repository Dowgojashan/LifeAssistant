package com.example.life_assistant.ViewModel

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
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> get() = _chatMessages

    private val _taskInfoList = MutableStateFlow<List<TaskInfo>>(emptyList()) // 儲存行程資訊
    val taskInfoList: StateFlow<List<TaskInfo>> get() = _taskInfoList

    data class TaskInfo(val name: String, val duration: String, val endTime: String)

    private fun Message.toChatGptMessage(): Message {
        return Message(role = this.role, content = this.content)
    }

    fun sendMessage(userMessage: String) {
        val currentMessages = _chatMessages.value.toMutableList()
        currentMessages.add(Message(role = "user", content = userMessage))
        _chatMessages.value = currentMessages

        // 整理完整的對話歷史
        val chatHistory = currentMessages.map { it.toChatGptMessage() }

        // 新增 Prompt
        val systemPrompt = """
        你是一個專門幫助使用者提取行程資訊的助手，請根據以下規則回應：
        1. 如果使用者描述的任務缺少完成期限（deadline），請在回應中提醒使用者提供該任務的完成期限。
        2. 如果使用者描述的任務缺少時長（duration），請在回應中提醒使用者提供該任務的時長。
        3. 如果使用者只輸入了任務名稱（例如：「我要打羽球」），請提醒使用者補充時長和完成期限，並指出需要完整的資訊。
        4. 如果使用者的訊息不是與行程相關的問題，請回應：「此聊天室僅專注於協助使用者安排行程，請輸入與行程相關的資訊。」
        5. 如果使用者是接續先前的輸入補充資訊（例如：先描述任務但後續才補上完成期限(確切日期)或時長(小時)），請結合當前訊息與之前的內容整合完整的資訊，並按照以下 JSON 格式輸出：
        [
          {"name": "任務名稱", "duration": "時長（如 3小時）", "endTime": "完成期限（如 11-30）"}
        ]
        6. 如果使用者的補充訊息無法與前一條訊息對應，請提醒使用者提供完整的行程資訊。
        7. 如果使用者在任務描述中使用了「明天」「後天」「下禮拜一」等相對時間描述，請將這些相對時間自動解析為具體日期，並視為完成期限，無需要求使用者再次輸入完成期限。
        8. 如果使用者同時提供了相對時間描述（如「明天」）與時長（如「三小時」），請直接整合並輸出完整的 JSON 資訊。
    """.trimIndent()

        val request = ChatGptRequest(
            messages = listOf(
                Message(role = "system", content = systemPrompt)
            ) + chatHistory // 加入完整歷史對話
        )

        RetrofitClient.instance.sendMessage(request).enqueue(object : Callback<ChatGptResponse> {
            private fun parseTaskInfoFromReply(reply: String): List<TaskInfo> {
                val taskList = mutableListOf<TaskInfo>()

                try {
                    // 找到 JSON 部分並提取
                    val jsonStartIndex = reply.indexOf("[") // 找到 JSON 開始的位置
                    val jsonEndIndex = reply.lastIndexOf("]") // 找到 JSON 結束的位置

                    if (jsonStartIndex != -1 && jsonEndIndex != -1) {
                        val jsonString = reply.substring(jsonStartIndex, jsonEndIndex + 1) // 提取 JSON 區段

                        // 解析 JSON
                        val jsonArray = JSONArray(jsonString)
                        for (i in 0 until jsonArray.length()) {
                            val taskJson = jsonArray.getJSONObject(i)
                            val name = taskJson.optString("name", "未知")
                            val duration = taskJson.optString("duration", "未知")
                            val endTime = taskJson.optString("endTime", "未知")
                            taskList.add(TaskInfo(name, duration, endTime))
                        }
                    } else {
                        Log.e("parseTaskInfoFromReply", "No valid JSON found in reply.")
                    }
                } catch (e: Exception) {
                    Log.e("parseTaskInfoFromReply", "Error parsing task info: ${e.message}")
                }

                return taskList
            }


            override fun onResponse(
                call: Call<ChatGptResponse>,
                response: Response<ChatGptResponse>
            ) {
                if (response.isSuccessful) {
                    val chatGptReply = response.body()?.choices?.firstOrNull()?.message?.content ?: "無回應"

                    // 嘗試解析 ChatGpt 的回應以生成 TaskInfo
                    val taskInfoList = parseTaskInfoFromReply(chatGptReply) // 動態解析

                    if (taskInfoList.isNotEmpty()) {
                        _taskInfoList.value = taskInfoList // 更新行程資訊
                    }

                    // 更新訊息列表
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
