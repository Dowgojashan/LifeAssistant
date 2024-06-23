package com.example.life_assistant.data

data class Event(
    val title: String = "",
    val description: String = "",
    val date: String = "", // 儲存日期為字串，簡化處理
    val time: String = "" // 儲存時間為字串
)