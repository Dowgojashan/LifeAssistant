package com.example.life_assistant.model

data class ChatGptRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>
)

data class Message(
    val role: String,  // "system", "user", "assistant"
    val content: String
)