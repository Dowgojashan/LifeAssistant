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

        Log.d("UUID", repeatGroupId)

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

            // 根據 repeatType 和 repeatEndDate 新增重複事件
            if (repeatType != "無") {
                val endDate = parseDate(repeatEndDate)
                val initialStartDateTime = parseEventDateTime(startTime)
                val initialEndDateTime = parseEventDateTime(endTime)
                var nextStartDateTime = when (repeatType) {
                    "每日" -> initialStartDateTime.plusDays(1)
                    "每週" -> initialStartDateTime.plusWeeks(1)
                    "每月" -> initialStartDateTime.plusMonths(1)
                    "每年" -> initialStartDateTime.plusYears(1)
                    else -> initialStartDateTime
                }
                var nextEndDateTime = when (repeatType) {
                    "每日" -> initialEndDateTime.plusDays(1)
                    "每週" -> initialEndDateTime.plusWeeks(1)
                    "每月" -> initialEndDateTime.plusMonths(1)
                    "每年" -> initialEndDateTime.plusYears(1)
                    else -> initialEndDateTime
                }

                while (nextStartDateTime.toLocalDate() <= endDate) {
                    val newRepeatEvent = event.copy(
                        startTime = formatEventDateTime(nextStartDateTime),
                        endTime = formatEventDateTime(nextEndDateTime)
                    )
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

                    nextStartDateTime = when (repeatType) {
                        "每日" -> nextStartDateTime.plusDays(1)
                        "每週" -> nextStartDateTime.plusWeeks(1)
                        "每月" -> nextStartDateTime.plusMonths(1)
                        "每年" -> nextStartDateTime.plusYears(1)
                        else -> nextStartDateTime
                    }
                    nextEndDateTime = when (repeatType) {
                        "每日" -> nextEndDateTime.plusDays(1)
                        "每週" -> nextEndDateTime.plusWeeks(1)
                        "每月" -> nextEndDateTime.plusMonths(1)
                        "每年" -> nextEndDateTime.plusYears(1)
                        else -> nextEndDateTime
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

    fun parseEventDateTime(dateTimeString: String): LocalDateTime {
        val cleanedString = dateTimeString.replace("T", " ")
        val eventFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return LocalDateTime.parse(cleanedString.trim(), eventFormatter)
    }

    fun formatEventDateTime(dateTime: LocalDateTime): String {
        val eventFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return dateTime.format(eventFormatter)
    }

    private fun parseDate(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        return LocalDate.parse(date, formatter)
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
    fun updateEvent(
        uid: String,
        name: String,
        startTime: String,
        endTime: String,
        tags: String,
        alarmTime: String,
        repeatEndDate: String,
        repeatType: String,
        description: String,
        currentMonth: LocalDate? = null,
        updateAll: Boolean
    ) {
        // 確保用戶已登入
        val memberId = auth.currentUser?.uid ?: return
        val date = parseEventDate(startTime).toString()
        val eventRef = database.getReference("members").child(memberId).child("events").child(uid)
        Log.d("test", "$updateAll")

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

        if (updateAll) {
            // 查找所有重複事件
            eventRef.get().addOnSuccessListener { snapshot ->
                val event = snapshot.getValue(Event::class.java) ?: return@addOnSuccessListener
                val eventGroupId = event.repeatGroupId

                // 刪除所有具有相同 repeatGroupId 的事件
                val eventsRef = database.getReference("members").child(memberId).child("events")
                eventsRef.orderByChild("repeatGroupId").equalTo(eventGroupId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.children.forEach { child ->
                            val childRef = child.ref
                            childRef.removeValue().addOnSuccessListener {
                                Log.d("Firebase", "Deleted event with uid: ${child.key}")
                            }.addOnFailureListener { exception ->
                                handleException(exception, "Unable to delete event with uid: ${child.key}")
                            }
                        }
                        Log.d("Firebase", "All repeat events deleted successfully")

                        // 根據 repeatType 和 repeatEndDate 新增重複事件
                        if (repeatType != "無") {
                            val endDate = parseDate(repeatEndDate)
                            val initialStartDateTime = parseEventDateTime(startTime)
                            val initialEndDateTime = parseEventDateTime(endTime)
                            var nextStartDateTime = initialStartDateTime
                            var nextEndDateTime = initialEndDateTime

                            while (nextStartDateTime.toLocalDate() <= endDate) {
                                val newRepeatEvent = event.copy(
                                    startTime = formatEventDateTime(nextStartDateTime),
                                    endTime = formatEventDateTime(nextEndDateTime)
                                )
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

                                nextStartDateTime = when (repeatType) {
                                    "每日" -> nextStartDateTime.plusDays(1)
                                    "每週" -> nextStartDateTime.plusWeeks(1)
                                    "每月" -> nextStartDateTime.plusMonths(1)
                                    "每年" -> nextStartDateTime.plusYears(1)
                                    else -> nextStartDateTime
                                }
                                nextEndDateTime = when (repeatType) {
                                    "每日" -> nextEndDateTime.plusDays(1)
                                    "每週" -> nextEndDateTime.plusWeeks(1)
                                    "每月" -> nextEndDateTime.plusMonths(1)
                                    "每年" -> nextEndDateTime.plusYears(1)
                                    else -> nextEndDateTime
                                }
                            }
                        }

                        getEventsForDate(date)
                        currentMonth?.let { getEventsForMonth(it) }
                    } else {
                        Log.d("Firebase", "No matching repeat events found for repeatGroupId: $eventGroupId")
                        getEventsForDate(date)
                        currentMonth?.let { getEventsForMonth(it) }
                    }
                }.addOnFailureListener { exception ->
                    handleException(exception, "Unable to delete all repeat events")
                }
            }.addOnFailureListener { exception ->
                handleException(exception, "Unable to find event for updating")
            }
        } else {
            // 只更新當前事件
            eventRef.updateChildren(updatedEvent).addOnSuccessListener {
                Log.d("Firebase", "Updated event successfully")
                getEventsForDate(date)
                currentMonth?.let { getEventsForMonth(it) }
            }.addOnFailureListener { exception ->
                handleException(exception, "Unable to update event")
            }
        }
    }


    //抓錯誤
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
    }

}