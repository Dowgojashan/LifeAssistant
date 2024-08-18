package com.example.life_assistant.data

//firebase使用
data class Member(
    val name: String = "",
    val birthday: String = "",
    val sleepTime: String = "",
    val wakeTime: String = "",
    val habit: String = "",
    val readingTag: String = "",
    val sportTag: String = "",
    val workTag: String = "",
    val leisureTag: String = "",
    val houseworkTag: String = "",
)
