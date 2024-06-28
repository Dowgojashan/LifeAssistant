package com.example.life_assistant.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "EventEntity",
    foreignKeys = [ForeignKey(
        entity = MemberEntity::class,
        parentColumns = ["uid"],
        childColumns = ["memberuid"],
        onDelete = ForeignKey.CASCADE
    )])
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerialName("memberuid")
    val memberuid: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("description")
    val description: String = "",

    @SerialName("date")
    val date: String,

    @SerialName("label")
    val label: String = "",

    //提醒時間
    @SerialName("remind_time")
    val remind_time: Long,

    //紀錄是哪種重複
    @SerialName("repeat")
    val repeat: Int,
){
    // Event 轉換成 EventEntity
    constructor(event: Event) : this(
        name = event.name,
        description = event.description,
        date = event.date,
        remind_time = event.remind_time,
        repeat = event.repeat
    )
}