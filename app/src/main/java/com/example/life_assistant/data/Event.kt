package com.example.life_assistant.data

data class Event(
    var uid: String = "",
    val name: String = "",
    val startTime: String,
    val endTime: String,
    val tags: String = "",
    val alarmTime: String,
    var repeatEndDate: String = "",
    var repeatType: String = "",
    val repeatGroupId: String = "",
    val duration: String = "", //選擇自動排程，該事件需要做多久時間
    val shortestTime: String = "", //如有切割，一個單位最少多長
    val longestTime: String = "", //如有切割，一個單位最長多長
    val description: String = "",
){
    // 建構子
    constructor() : this("", "", "", "", "", "","","")


    // EventEntity 改變成 Event
    constructor(eventEntity: EventEntity) : this(
        name = eventEntity.name,
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
