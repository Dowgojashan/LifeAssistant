package com.example.life_assistant.Repository

import android.util.Log
import com.example.life_assistant.Event
import com.example.life_assistant.data.EventEntity
import com.example.life_assistant.data.MemberEntity
import com.example.life_assistant.datasource.EventDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface EventRepository{
    suspend fun insert(eventEntity: EventEntity)

    suspend fun  delete(eventEntity: EventEntity)

    suspend fun  update(eventEntity: EventEntity)

    suspend fun getEventsByDate(uid: String, date: String): Flow<List<EventEntity>>

    suspend fun getByid(id: Int): EventEntity?
}

class EventRepositoryImpl @Inject constructor(
    private val edao:EventDao,
) : EventRepository{
    override suspend fun insert(eventEntity: EventEntity) {
        withContext(Dispatchers.IO) {
            edao.insert(eventEntity)
        }
    }

    override suspend fun delete(eventEntity: EventEntity) {
        withContext(Dispatchers.IO) {
            Log.d("test","$eventEntity")
            edao.delete(eventEntity)
        }
    }

    override suspend fun update(eventEntity: EventEntity) {
        withContext(Dispatchers.IO) {
            edao.update(eventEntity)
        }
    }

    override suspend fun getEventsByDate(uid: String, date: String): Flow<List<EventEntity>> {
        return withContext(Dispatchers.IO) {
            val eventsFlow = edao.getEventsByDate(uid, date)
            Log.d("test1", "events: $eventsFlow")
            eventsFlow.onEach { events ->
                Log.d("test1", "Fetched events: $events")
            }
        }
    }

    override suspend fun getByid(id: Int): EventEntity? {
        return edao.getByid(id)
    }
}