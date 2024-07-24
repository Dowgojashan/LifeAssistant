package com.example.life_assistant.data

data class Event(
    var uid: String = "",
    val name: String = "",
    val date: String,
    val startTime: String,
    val endTime: String,
    val tags: String = "",
    val alarmTime: String,
    var repeatEndDate: String = "", // 新增字段
    var repeatType: String = "",
    val repeatGroupId: String = "",
    val description: String = "",
){
    // 建構子
    constructor() : this("", "", "", "", "", "","","")


    // EventEntity 改變成 Event
    constructor(eventEntity: EventEntity) : this(
        name = eventEntity.name,
        date = eventEntity.date,
        startTime = eventEntity.startTime,
        endTime = eventEntity.endTime,
        tags = eventEntity.tags,
        alarmTime = eventEntity.alarmTime,
        repeatEndDate = eventEntity.repeatEndDate,
        repeatType = eventEntity.repeatType,
        repeatGroupId = eventEntity.repeatGroupId,
        description = eventEntity.description,
    )
}
