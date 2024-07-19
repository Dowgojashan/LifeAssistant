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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    val auth: FirebaseAuth,
): ViewModel(){
    val database = FirebaseDatabase.getInstance("https://life-assistant-27ae8-default-rtdb.europe-west1.firebasedatabase.app/")
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events
    private val _eventsByDate = MutableStateFlow<List<EventEntity>>(emptyList())
    val eventsByDate: StateFlow<List<EventEntity>> = _eventsByDate


    //firebase新增事件
    fun addEvent(name: String, date: Long, startTime: String, endTime: String, tags: String, alarmTime: String, repeat: String, description: String,currentMonth: LocalDate? = null) {
        val memberId = auth.currentUser?.uid ?: return
        val formatteddate = convertLongToDate(date)

        // Room Database 物件
        val newEvent = EventEntity(
            memberuid = memberId,
            name = name,
            date = formatteddate,
            startTime = startTime,
            endTime = endTime,
            tags = tags,
            alarmTime = alarmTime,
            repeat = repeat,
            description = description,
        )

        // Firebase 物件
        val event = Event(
            name = name,
            date = formatteddate,
            startTime = startTime,
            endTime = endTime,
            tags = tags,
            alarmTime = alarmTime,
            repeat = repeat,
            description = description,
        )

        val eventRef: DatabaseReference = database.getReference("members")
            .child(memberId)
            .child("events")
            .push()

        val eventUid = eventRef.key // 獲取自動生成的 UID

        // 設置 Event 物件的 UID
        event.uid = eventUid ?: ""

        // 設置 Firebase 中的 Event
        eventRef.setValue(event).addOnSuccessListener {
            Log.d("Firebase", "Event saved successfully with UID: $eventUid")
            var replace = formatteddate.replace("\n", "")
            // 新增 UID 到 Room Database 物件中
//            val newEventWithUid = newEvent.copy(uid = eventUid ?: "")
//            insertEvent(newEventWithUid) // 儲存到 Room 資料庫
            getEventsForDate(replace)
            if (currentMonth != null) {
                getEventsForMonth(currentMonth)
            }
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

    // 從firebase取得事件
    fun getEventsForDate(date: String) {
        val replacedate = date.replace("年", "年\n")
        val memberId = auth.currentUser?.uid ?: return
        val eventRef = database.getReference("members").child(memberId).child("events")

        eventRef.get().addOnSuccessListener { snapshot ->
            val eventsList = mutableListOf<Event>()
            for (data in snapshot.children) {
                // 讀取 Firebase 數據
                val eventMap = data.value as? Map<*, *>
                if (eventMap != null) {
                    val uid = eventMap["uid"] as? String ?: ""
                    val name = eventMap["name"] as? String ?: ""
                    val dateFromFirebase = eventMap["date"] as? String ?: ""
                    val startTime = eventMap["startTime"] as? String ?: ""
                    val endTime = eventMap["endTime"] as? String ?: ""
                    val tags = eventMap["tags"] as? String ?: ""
                    val alarmTime = eventMap["alarmTime"] as? String ?: ""
                    val repeat = eventMap["repeat"] as? String ?: ""
                    val description = eventMap["description"] as? String ?: ""

                    // 創建 Event 物件
                    val event = Event(
                        uid = uid,
                        name = name,
                        date = dateFromFirebase,
                        startTime = startTime,
                        endTime = endTime,
                        tags = tags,
                        alarmTime = alarmTime,
                        repeat = repeat,
                        description = description
                    )

                    if (event.date == replacedate) {
                        eventsList.add(event)
                    }
                }
            }
            _eventsByDate.value = eventsList.map { EventEntity(it) }
            _events.value = eventsList  // 更新 MutableLiveData
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events for date $date")
            // Firebase 失敗時從 Room 取得資料
            viewModelScope.launch {
                Log.d("test","roomdatabase")
                getEventsByDate(date)
            }
        }
    }

    //從roomdatabase取得事件
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
                getEventsForDate(date)
            }
        }
    }

    //取得這個月所有事件
    fun getEventsForMonth(month: LocalDate) {
        val year = month.year
        val monthValue = month.monthValue
        val memberId = auth.currentUser?.uid ?: return
        val eventRef = database.getReference("members").child(memberId).child("events")

        eventRef.get().addOnSuccessListener { snapshot ->
            val eventsList = mutableListOf<Event>()
            for (data in snapshot.children) {
                // 讀取 Firebase 數據
                val eventMap = data.value as? Map<*, *>
                if (eventMap != null) {
                    val uid = eventMap["uid"] as? String ?: ""
                    val name = eventMap["name"] as? String ?: ""
                    val dateFromFirebase = eventMap["date"] as? String ?: ""
                    val startTime = eventMap["startTime"] as? String ?: ""
                    val endTime = eventMap["endTime"] as? String ?: ""
                    val tags = eventMap["tags"] as? String ?: ""
                    val alarmTime = eventMap["alarmTime"] as? String ?: ""
                    val repeat = eventMap["repeat"] as? String ?: ""
                    val description = eventMap["description"] as? String ?: ""

                    // 創建 Event 物件
                    val event = Event(
                        uid = uid,
                        name = name,
                        date = dateFromFirebase,
                        startTime = startTime,
                        endTime = endTime,
                        tags = tags,
                        alarmTime = alarmTime,
                        repeat = repeat,
                        description = description
                    )

                    // 判斷是否在當前月份
                    val eventDate = LocalDate.parse(dateFromFirebase, DateTimeFormatter.ofPattern("yyyy年\nM月d日"))
                    if (eventDate.year == year && eventDate.monthValue == monthValue) {
                        eventsList.add(event)
                    }
                }
            }
            _eventsByDate.value = eventsList.map { EventEntity(it) }
            _events.value = eventsList  // 更新 MutableLiveData
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events for month $month")
            // Firebase 失敗時從 Room 取得資料
            viewModelScope.launch {
                Log.d("test", "roomdatabase")
                //getEventsForMonthFromRoom(month) // 從 Room 取得當月事件
            }
        }
    }

//    fun getEventsForMonthFromRoom(month: LocalDate) {
//        // 這是從 Room 取得事件的示例方法
//        // 需要實現這個方法以根據月份取得事件
//        viewModelScope.launch {
//            val startOfMonth = month.withDayOfMonth(1)
//            val endOfMonth = month.withDayOfMonth(month.length(month.isLeapYear))
//            val events = eventDao.getEventsBetweenDates(startOfMonth, endOfMonth)
//            _eventsByDate.value = events.map { EventEntity(it) }
//            _events.value = events.map { it.toEvent() }  // 假設有 toEvent() 擴展方法將 EventEntity 轉換為 Event
//        }
//    }

    //roomdatabase刪除事件
    fun deleteEventFromRoom(event: EventEntity) {
        viewModelScope.launch {
            val memberId = auth.currentUser?.uid ?: return@launch
            val eventWithUid = event.copy(memberuid = memberId)
            val date = event.date
            eventRepository.delete(eventWithUid)
            getEventsForDate(date)
        }
    }

    //firebase刪除
    fun deleteEventFromFirebase(event: Event,temp: String,onSuccess: () -> Unit) {
        val memberId = auth.currentUser?.uid ?: return
        val eventUid = event.uid
        val eventRef = database.getReference("members").child(memberId).child("events")
        Log.d("uid",eventUid)

        eventRef.child(eventUid).removeValue().addOnSuccessListener {
            Log.d("Firebase", "Event deleted successfully")
            val replacedDate = event.date.replace("\n", "")
            if(temp == "daily"){
                getEventsForDate(replacedDate) // 更新 UI，顯示新的事件列表
            }
            else if(temp == "month"){
                val eventDate = LocalDate.parse(replacedDate,DateTimeFormatter.ofPattern("yyyy年M月d日")) // 解析日期字串為 LocalDate
                getEventsForMonth(eventDate)
            }
            onSuccess() // 成功刪除後呼叫 onSuccess 回調

        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to delete event")
        }
    }

    fun deleteEvent(event: EventEntity) {
        val firebaseEvent = Event(event)
        deleteEventFromRoom(event)
        //deleteEventFromFirebase(firebaseEvent)
    }

    //修改事件
    fun updateEvent(uid: String,name: String,date: Long, startTime: String, endTime: String, tags: String, alarmTime: String, repeatSetting: String, description: String,currentMonth: LocalDate? = null) {
        // 確保用戶已登入
        val memberId = auth.currentUser?.uid ?: return
        val formatteddate = convertLongToDate(date)

        // Firebase 的事件參考路徑
        val eventRef = database.getReference("members").child(memberId).child("events").child(uid)

        // 將要更新的事件資料
        val updatedEvent = mapOf(
            "name" to name,
            "date" to formatteddate,
            "startTime" to startTime,
            "endTime" to endTime,
            "tags" to tags,
            "alarmTime" to alarmTime,
            "repeat" to repeatSetting,
            "description" to description
        )

        // 開始更新 Firebase 中的事件資料
        eventRef.updateChildren(updatedEvent).addOnSuccessListener {
            Log.d("firebase","updated successfully")
            val replace = formatteddate.replace("\n", "")
            Log.d("firebase",replace)
            getEventsForDate(replace)

            if (currentMonth != null) {
                getEventsForMonth(currentMonth)
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "無法更新事件資料")
        }
    }

    //抓錯誤
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
    }

}