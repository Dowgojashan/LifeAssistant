package com.example.life_assistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//room database使用
@Serializable
@Entity(tableName = tableName)
data class MemberEntity(
    @PrimaryKey
    @SerialName("uid")
    val uid: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("birthday")
    val birthday: String = "",

    @SerialName("wake_time")
    val wake_time: String = "",

    @SerialName("sleep_time")
    val sleep_time: String = "",
)

const val tableName = "MemberEntity"
