package com.example.life_assistant.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.life_assistant.Repository.EventRepository
import com.example.life_assistant.Screen.convertLongToDate
import com.example.life_assistant.data.Event
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.life_assistant.data.EventEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    val auth: FirebaseAuth,
): ViewModel(){
    val database = FirebaseDatabase.getInstance("https://life-assistant-27ae8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val _events = MutableLiveData<List<EventEntity>>()
    val events: LiveData<List<EventEntity>> = _events


    //firebase新增事件
    fun addEvent(name: String,description: String,date: Long){
        val memberId = auth.currentUser?.uid ?: return
        val formatteddate = convertLongToDate(date)
        val newEvent = EventEntity(
            memberuid = memberId,
            name = name,
            description = description,
            date = formatteddate,
            remind_time = 0L,
            repeat = 0
        )
        val event = Event(name, description, formatteddate, remind_time = 0, label = "", repeat = 0)
        val events = mutableStateListOf<Event>()
        events.add(event)

        // 將事件保存到 Firebase
        val eventRef = database.getReference("members").child(memberId).child("events").push()
        eventRef.setValue(event).addOnSuccessListener {
            Log.d("Firebase", "Event saved successfully")
            insertEvent(newEvent)
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to save event")
        }
    }

    //roomdatabase新增事件
    fun insertEvent(event: EventEntity){
        viewModelScope.launch {
            eventRepository.insert(event)
        }
    }

    // 獲取特定日期的事件列表
    fun getEventsForDate(date: String): List<Event> {
        val memberId = auth.currentUser?.uid ?: return emptyList()
        val eventRef = database.getReference("members").child(memberId).child("events")
        val events = mutableStateListOf<Event>()

        eventRef.get().addOnSuccessListener { snapshot ->
            events.clear()
            for (data in snapshot.children) {
                val event = data.getValue(Event::class.java)
                if (event?.date == date) {
                    event?.let {
                        Log.d("test","firebase")
                        events.add(it)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events for date $date")
            // Firebase 失敗時從 Room 取得資料
            viewModelScope.launch {
                Log.d("test","roomdatabase")
                getEventsByDate(date)
            }
        }
        return events
    }

    private val _eventsByDate = MutableStateFlow<List<EventEntity>>(emptyList())
    val eventsByDate: StateFlow<List<EventEntity>> = _eventsByDate
    //取得事件
    fun getEventsByDate(date: String) {
        val replacedate = date.replace("年0", "年\n0").replace("年\n0", "年\n").replace("年\n0", "年\n")
        Log.d("test", replacedate)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                eventRepository.getEventsByDate(auth.currentUser?.uid ?: "", replacedate)
                    .collect { eventList ->
                        Log.d("test", "Received events: $eventList")
                        _eventsByDate.value = eventList
                    }
            } catch (e: Exception) {
                Log.e("test", "Error collecting events", e)
                // Handle error situation here, if needed
            }
        }
    }

    //刪除事件
    fun deleteEventFromRoom(event: EventEntity) {
        viewModelScope.launch {
            eventRepository.delete(event)
        }
    }

    //firebase刪除
    fun deleteEventFromFirebase(event: Event) {
        val memberId = auth.currentUser?.uid ?: return
        val eventRef = database.getReference("members").child(memberId).child("events")

        eventRef.get().addOnSuccessListener { snapshot ->
            for (data in snapshot.children) {
                val existingEvent = data.getValue(Event::class.java)
                if (existingEvent != null && existingEvent.name == event.name && existingEvent.description == event.description) {
                    data.ref.removeValue().addOnSuccessListener {
                        Log.d("Firebase", "Event deleted successfully")
                    }.addOnFailureListener { exception ->
                        handleException(exception, "Unable to delete event")
                    }
                }
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events for deletion")
        }
    }

    fun deleteEvent(event: EventEntity) {
        val firebaseEvent = Event(event)
        deleteEventFromRoom(event)
        deleteEventFromFirebase(firebaseEvent)
    }

    //抓錯誤
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
    }
}