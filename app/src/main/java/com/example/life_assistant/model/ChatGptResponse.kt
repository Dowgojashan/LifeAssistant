package com.example.life_assistant.model

data class ChatGptResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)