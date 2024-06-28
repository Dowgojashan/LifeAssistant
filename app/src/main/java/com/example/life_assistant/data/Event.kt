package com.example.life_assistant.data

data class Event(
    val name: String = "",
    val description: String = "",
    val date: String,
    val label: String = "",
    val remind_time: Long,
    val repeat: Int,
){
    // 建構子
    constructor() : this("", "", "", "", 0L, 0)


    // EventEntity 改變成 Event
    constructor(eventEntity: EventEntity) : this(
        name = eventEntity.name,
        description = eventEntity.description,
        date = eventEntity.date,
        remind_time = eventEntity.remind_time,
        repeat = eventEntity.repeat
    )
}
