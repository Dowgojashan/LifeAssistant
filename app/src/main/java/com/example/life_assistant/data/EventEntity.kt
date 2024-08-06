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

    @SerialName("date")
    val date: String,

    @SerialName("startTime")
    val startTime: String,

    @SerialName("endTime")
    val endTime: String,

    @SerialName("label")
    val tags: String = "",

    //提醒時間
    @SerialName("remind_time")
    val alarmTime: String,

    //紀錄重覆到哪一天
    @SerialName("repeatEndDate")
    val repeatEndDate: String,

    //紀錄是哪種重複
    @SerialName("repeatType")
    val repeatType: String,

    //紀錄是哪一組的重複
    @SerialName("repeatGroupId")
    val repeatGroupId: String,

    @SerialName("duration")
    val duration: String,

    @SerialName("shortestTime")
    val shortestTime: String,

    @SerialName("longestTime")
    val longestTime: String,

    @SerialName("description")
    val description: String = "",
){
    // Event 轉換成 EventEntity
//    constructor(event: Event) : this(
//        name = event.name,
//        startTime = event.startTime,
//        endTime = event.endTime,
//        tags = event.tags,
//        alarmTime = event.alarmTime,
//        repeatEndDate = event.repeatEndDate,
//        repeatType = event.repeatType,
//        repeatGroupId = event.repeatGroupId,
//        description = event.description,
//    )
}