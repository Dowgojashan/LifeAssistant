package com.example.life_assistant.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.life_assistant.data.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventEntity: EventEntity)

    @Delete
    suspend fun delete(eventEntity: EventEntity)

    @Update
    suspend fun update(eventEntity: EventEntity)

    @Query("SELECT * FROM EventEntity WHERE memberuid = :uid AND date = :date")
    fun getEventsByDate(uid: String, date: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM EventEntity WHERE id = :id LIMIT 1")
    fun getByid(id: Int): EventEntity?
}