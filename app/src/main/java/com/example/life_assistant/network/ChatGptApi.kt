package com.example.life_assistant.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import com.example.life_assistant.model.ChatGptRequest
import com.example.life_assistant.model.ChatGptResponse

interface ChatGptApi {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer sk-4ERRCb6ORRcfAkSnnEwuDgnHwOpL5FPkIDb4X1qOwLRgnfOL"
    )
    @POST("v1/chat/completions")
    fun sendMessage(@Body request: ChatGptRequest): Call<ChatGptResponse>
}