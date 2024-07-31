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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


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


    //新增事件
    fun addEvent(
        name: String,
        startTime: String,
        endTime: String,
        tags: String,
        alarmTime: String,
        repeatEndDate: String,
        repeatType: String,
        description: String,
        currentMonth: LocalDate? = null
    ) {
        val memberId = auth.currentUser?.uid ?: return
        val date = "2024/07/31"

        // 生成唯一的 repeatGroupId
        val repeatGroupId = if (repeatType != "無") {
            UUID.randomUUID().toString()  // 使用 UUID 生成唯一的 repeatGroupId
        } else {
            ""  // 不使用 repeatGroupId
        }

        Log.d("UUID",repeatGroupId)

        // Room Database 物件
//        val newEvent = EventEntity(
//            memberuid = memberId,
//            name = name,
//            startTime = startTime,
//            endTime = endTime,
//            tags = tags,
//            alarmTime = alarmTime,
//            repeatEndDate = repeatEndDate,
//            repeatType = repeatType,
//            repeatGroupId = repeatGroupId,
//            description = description,
//        )

        // Firebase 物件
        val event = Event(
            name = name,
            startTime = startTime,
            endTime = endTime,
            tags = tags,
            alarmTime = alarmTime,
            repeatEndDate = repeatEndDate,
            repeatType = repeatType,
            repeatGroupId = repeatGroupId,
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
            // 新增 UID 到 Room Database 物件中
            // val newEventWithUid = newEvent.copy(uid = eventUid ?: "")
            // insertEvent(newEventWithUid) // 儲存到 Room 資料庫

            // 根據 repeatType 和 repeatEndDate 新增重複事件
            if (repeatType != "無") {
                val endDate = parseDate(repeatEndDate)
                val initialDate = parseDate(date.replace("年\n", "-").replace("月", "-").replace("日", ""))
                var nextDate = when (repeatType) {
                    "每日" -> initialDate.plusDays(1)
                    "每週" -> initialDate.plusWeeks(1)
                    "每月" -> initialDate.plusMonths(1)
                    "每年" -> initialDate.plusYears(1)
                    else -> initialDate
                }

                while (nextDate <= endDate) {
                    val newRepeatEvent = event.copy(startTime = formatDate(nextDate))
                    val newRepeatEventRef = database.getReference("members")
                        .child(memberId)
                        .child("events")
                        .push()
                    newRepeatEvent.uid = newRepeatEventRef.key ?: ""
                    newRepeatEventRef.setValue(newRepeatEvent).addOnSuccessListener {
                        Log.d("Firebase", "Repeat event saved successfully with UID: ${newRepeatEvent.uid}")
                    }.addOnFailureListener { exception ->
                        handleException(exception, "Unable to save repeat event")
                    }

                    nextDate = when (repeatType) {
                        "每日" -> nextDate.plusDays(1)
                        "每週" -> nextDate.plusWeeks(1)
                        "每月" -> nextDate.plusMonths(1)
                        "每年" -> nextDate.plusYears(1)
                        else -> nextDate
                    }
                }
            }

            getEventsForDate(parseEventDate(startTime).toString())
            if (currentMonth != null) {
                getEventsForMonth(currentMonth)
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to save event")
        }
    }

    private fun parseDate(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        return LocalDate.parse(date, formatter)
    }

    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy年\nM月d日")
        return date.format(formatter)
    }

    //roomdatabase新增事件
    fun insertEvent(event: EventEntity){
        viewModelScope.launch {
            eventRepository.insert(event)
        }
    }

    // 從 Firebase 取得事件
    fun getEventsForDate(date: String) {
        val memberId = auth.currentUser?.uid ?: return
        val eventRef = database.getReference("members").child(memberId).child("events")

        eventRef.get().addOnSuccessListener { snapshot ->
            val eventsList = mutableListOf<Event>()
            val targetDate = parseCurrentDay(date)

            for (data in snapshot.children) {
                // 讀取 Firebase 數據
                val eventMap = data.value as? Map<*, *>
                if (eventMap != null) {
                    val uid = eventMap["uid"] as? String ?: ""
                    val name = eventMap["name"] as? String ?: ""
                    val startTime = eventMap["startTime"] as? String ?: ""
                    val endTime = eventMap["endTime"] as? String ?: ""
                    val tags = eventMap["tags"] as? String ?: ""
                    val alarmTime = eventMap["alarmTime"] as? String ?: ""
                    val repeatEndDate = eventMap["repeatEndDate"] as? String ?: ""
                    val repeatType = eventMap["repeatType"] as? String ?: ""
                    val repeatGroupId = eventMap["repeatGroupId"] as? String ?: ""
                    val description = eventMap["description"] as? String ?: ""

                    // 創建 Event 物件
                    val event = Event(
                        uid = uid,
                        name = name,
                        startTime = startTime,
                        endTime = endTime,
                        tags = tags,
                        alarmTime = alarmTime,
                        repeatEndDate = repeatEndDate,
                        repeatType = repeatType,
                        repeatGroupId = repeatGroupId,
                        description = description
                    )

                    // 確保事件的日期與今天的日期重疊
                    if (isDateInRange(targetDate, event.startTime, event.endTime)) {
                        eventsList.add(event)
                    }
                }
            }
            _events.value = eventsList  // 更新 MutableLiveData
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events for date $date")
            // Firebase 失敗時從 Room 取得資料
            viewModelScope.launch {
                Log.d("test", "roomdatabase")
                getEventsByDate(date)
            }
        }
    }

    fun isDateInRange(date: LocalDate, startTime: String, endTime: String): Boolean {
        val eventStartDate = parseEventDate(startTime)
        val eventEndDate = parseEventDate(endTime)

        // 檢查事件是否橫跨當前日期
        return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)
    }

    fun parseEventDate(dateString: String): LocalDate {
        val eventFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val localDateTime = LocalDateTime.parse(dateString.trim(), eventFormatter)
        return localDateTime.toLocalDate()
    }

    fun parseCurrentDay(dateString: String): LocalDate {
        val currentDayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(dateString.trim(), currentDayFormatter)
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
                val eventMap = data.value as? Map<*, *>
                if (eventMap != null) {
                    val uid = eventMap["uid"] as? String ?: ""
                    val name = eventMap["name"] as? String ?: ""
                    val startTime = eventMap["startTime"] as? String ?: ""
                    val endTime = eventMap["endTime"] as? String ?: ""
                    val tags = eventMap["tags"] as? String ?: ""
                    val alarmTime = eventMap["alarmTime"] as? String ?: ""
                    val repeatEndDate = eventMap["repeatEndDate"] as? String ?: ""
                    val repeatType = eventMap["repeatType"] as? String ?: ""
                    val repeatGroupId = eventMap["repeatGroupId"] as? String ?: ""
                    val description = eventMap["description"] as? String ?: ""

                    val event = Event(
                        uid = uid,
                        name = name,
                        startTime = startTime,
                        endTime = endTime,
                        tags = tags,
                        alarmTime = alarmTime,
                        repeatEndDate = repeatEndDate,
                        repeatType = repeatType,
                        repeatGroupId = repeatGroupId,
                        description = description
                    )

                    val eventStartDate = parseEventDate(event.startTime)
                    val eventEndDate = parseEventDate(event.endTime)

                    if (isDateInMonth(month, eventStartDate, eventEndDate)) {
                        eventsList.add(event)
                    }
                }
            }
            _events.value = eventsList
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events for month $month")
        }
    }

    fun isDateInMonth(month: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        // 確保事件的開始和結束時間都在指定的月份範圍內
        val monthStart = month.withDayOfMonth(1)
        val monthEnd = month.withDayOfMonth(month.lengthOfMonth())

        return !(endDate.isBefore(monthStart) || startDate.isAfter(monthEnd))
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
            eventRepository.delete(eventWithUid)
        }
    }

    //firebase刪除
    fun deleteEventFromFirebase(event: Event, temp: String, deleteAll: Boolean, onSuccess: () -> Unit) {
        val memberId = auth.currentUser?.uid ?: return
        val eventUid = event.uid
        val eventGroupId = event.repeatGroupId
        val date = parseEventDate(event.startTime).toString()
        val eventRef = database.getReference("members").child(memberId).child("events")
        Log.d("uid", eventGroupId)

        if (deleteAll) {
            // 刪除所有重複事件
            eventRef.orderByChild("repeatGroupId").equalTo(eventGroupId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {

                    Log.d("Firebase", "Snapshot exists: ${snapshot.childrenCount} items found")
                    snapshot.children.forEach { child ->
                        val uid = child.key ?: return@forEach
                        eventRef.child(uid).removeValue()
                    }
                    Log.d("Firebase", "All repeat events deleted successfully")
                } else {
                    Log.d("Firebase", "Attempting to delete events with repeatGroupId: $eventGroupId")
                    Log.d("Firebase", "No matching events found for repeatGroupId: ${event.repeatGroupId}")
                }
                if (temp == "daily") {
                    getEventsForDate(date) // 更新 UI，顯示新的事件列表
                } else if (temp == "month") {
                    val eventDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")) // 解析日期字串為 LocalDate
                    getEventsForMonth(eventDate)
                }
                onSuccess()
            }.addOnFailureListener { exception ->
                handleException(exception, "Unable to delete all repeat events")
            }
        } else {
            // 僅刪除單個事件
            eventRef.child(eventUid).removeValue().addOnSuccessListener {
                Log.d("Firebase", "Event deleted successfully")
                if (temp == "daily") {
                    getEventsForDate(date) // 更新 UI，顯示新的事件列表
                } else if (temp == "month") {
                    val eventDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")) // 解析日期字串為 LocalDate
                    getEventsForMonth(eventDate)
                }
                onSuccess()
            }.addOnFailureListener { exception ->
                handleException(exception, "Unable to delete event")
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        val firebaseEvent = Event(event)
        deleteEventFromRoom(event)
        //deleteEventFromFirebase(firebaseEvent)
    }

    //修改事件
    fun updateEvent(uid: String,name: String, startTime: String, endTime: String, tags: String, alarmTime: String, repeatEndDate: String ,repeatType:String, description: String,currentMonth: LocalDate? = null) {
        // 確保用戶已登入
        val memberId = auth.currentUser?.uid ?: return
        val date = parseEventDate(startTime).toString()
        // Firebase 的事件參考路徑
        val eventRef = database.getReference("members").child(memberId).child("events").child(uid)

        // 將要更新的事件資料
        val updatedEvent = mapOf(
            "name" to name,
            "startTime" to startTime,
            "endTime" to endTime,
            "tags" to tags,
            "alarmTime" to alarmTime,
            "repeatEndDate" to repeatEndDate,
            "repeatType" to repeatType,
            "description" to description
        )

        // 開始更新 Firebase 中的事件資料
        eventRef.updateChildren(updatedEvent).addOnSuccessListener {
            Log.d("firebase","updated successfully")
            getEventsForDate(date)

            Log.d("Month","$currentMonth")
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