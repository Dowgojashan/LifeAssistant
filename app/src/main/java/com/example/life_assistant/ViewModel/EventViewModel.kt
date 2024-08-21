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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.time.Duration

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
        duration: String,
        idealTime: String,
        shortestTime: String,
        longestTime: String,
        dailyRepeat: Boolean,
        disturb: Boolean,
        description: String,
        currentMonth: LocalDate? = null,
        edituid: String? = null
    ) {
        val memberId = auth.currentUser?.uid ?: return

        // 重複的uuid產生
        val repeatGroupId = if (repeatType != "無") {
            //如果為修改 就用原本的uuid
            if(edituid?.isNotBlank() == true){
                edituid
            }
            //如果是新增 就亂數產生新的uuid
            else{
                UUID.randomUUID().toString()  // 使用 UUID 生成唯一的 repeatGroupId
            }
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
            duration = duration,
            idealTime = idealTime,
            shortestTime = shortestTime,
            longestTime = longestTime,
            dailyRepeat = dailyRepeat,
            disturb = disturb,
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
                    val duration = eventMap["duration"] as? String ?: ""
                    val idealTime = eventMap["idealTime"] as? String ?: ""
                    val shortestTime = eventMap["shortestTime"] as? String ?: ""
                    val longestTime = eventMap["longestTime"] as? String ?: ""
                    val dailyRepeat = eventMap["dailyRepeat"] as? Boolean ?: false
                    val disturb = eventMap["disturb"] as? Boolean ?: false
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
                        duration = duration,
                        idealTime = idealTime,
                        shortestTime = shortestTime,
                        longestTime = longestTime,
                        dailyRepeat = dailyRepeat,
                        disturb = disturb,
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
                    val duration = eventMap["duration"] as? String ?: ""
                    val idealTime = eventMap["idealTime"] as? String ?: ""
                    val shortestTime = eventMap["shortestTime"] as? String ?: ""
                    val longestTime = eventMap["longestTime"] as? String ?: ""
                    val dailyRepeat = eventMap["dailyRepeat"] as? Boolean ?: false
                    val disturb = eventMap["disturb"] as? Boolean ?: false
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
                        duration = duration,
                        idealTime = idealTime,
                        shortestTime = shortestTime,
                        longestTime = longestTime,
                        dailyRepeat = dailyRepeat,
                        disturb = disturb,
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
        duration: String,
        idealTime: String,
        shortestTime: String,
        longestTime: String,
        dailyRepeat: Boolean,
        disturb: Boolean,
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
            "duration" to duration,
            "idealTime" to idealTime,
            "shortestTime" to shortestTime,
            "longestTime" to longestTime,
            "dailyRepeat" to dailyRepeat,
            "disturb" to disturb,
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
                        addEvent(name,startTime,endTime,tags,alarmTime,repeatEndDate,repeatType,duration,idealTime,shortestTime,longestTime,dailyRepeat,disturb,description,currentMonth,eventGroupId)
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

    private fun getWakeSleepTime(callback: (wakeTime: String, sleepTime: String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val memberRef = database.getReference("members").child(userId)
        memberRef.child("wakeTime").get().addOnSuccessListener { wakeTimeSnapshot ->
            val wakeTime = wakeTimeSnapshot.value as? String ?: "00:00" // 如果沒有值則設為預設時間
            memberRef.child("sleepTime").get().addOnSuccessListener { sleepTimeSnapshot ->
                val sleepTime = sleepTimeSnapshot.value as? String ?: "00:00" // 如果沒有值則設為預設時間

                callback(wakeTime, sleepTime)
            }.addOnFailureListener {
                // 處理失敗情況
                callback("00:00", "00:00")
            }
        }.addOnFailureListener {
            // 處理失敗情況
            callback("00:00", "00:00")
        }
    }

    fun getFreeTime(
        startTime: String,
        endTime: String,
        callback: (List<Pair<String, String>>) -> Unit
    ) {
        // 定義時間格式
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // 解析時間字符串
        val start = LocalDateTime.parse(startTime, dateTimeFormatter)
        val end = LocalDateTime.parse(endTime, dateTimeFormatter)
        val endofTime = end.toLocalTime()
        val endBoundary = LocalTime.parse("05:01", timeFormatter)
        val startBoundary = LocalTime.parse("00:00", timeFormatter)

        // 獲取起床和睡覺時間
        getWakeSleepTime { wakeTime, sleepTime ->
            val wake = LocalTime.parse(wakeTime, timeFormatter)
            val sleep = LocalTime.parse(sleepTime, timeFormatter)

            // 定義輸出格式
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

            // 計算日期範圍
            val startDate = start.toLocalDate()
            val endDate = end.toLocalDate()

            // 存要檢查的時間範圍
            val scheduleDates = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

            // 如果睡覺時間早於起床時間(解決跨日問題)
            if (sleep.isBefore(wake)) {
                //如果事件的開始時間是同一天(包含過午夜十二點的)
                if (startDate.isEqual(endDate) || (startDate.plusDays(1).isEqual(endDate) && endofTime.isBefore(endBoundary))) {
                    val startOfFirstDay = LocalDateTime.of(startDate,wake)
                    val endOfFirstDay = LocalDateTime.of(startDate, sleep)
                    val startTime = start.toLocalTime()
                    val endTime = end.toLocalTime()
                    if(startTime.isBefore(sleep) && startTime.isBefore(endBoundary) && end.isAfter(startOfFirstDay)){
                        scheduleDates.add(start to endOfFirstDay)

                        scheduleDates.add(startOfFirstDay to end)
                    }
                    else if(startTime.isAfter(sleep) && startTime.isBefore(wake) && startTime.isBefore(endBoundary)){
                        scheduleDates.add(startOfFirstDay to end)
                    }
                    else{
                        scheduleDates.add(start to end)
                    }
                }
                else {
                    // 第一天存開始到睡覺時間
                    val endOfFirstDay = LocalDateTime.of(startDate.plusDays(1), sleep)
                    val firstDaySleep = LocalDateTime.of(startDate,sleep)
                    val startOfFirstDay = LocalDateTime.of(startDate,wake)
                    val startTime = start.toLocalTime()
                    val endTime = end.toLocalTime()
                    if(startTime.isBefore(sleep) && startTime.isBefore(endBoundary) && end.isAfter(startOfFirstDay)){
                        scheduleDates.add(start to firstDaySleep)
                    }
                    else if(startTime.isAfter(sleep) && startTime.isBefore(wake) && startTime.isBefore(endBoundary)){
                        scheduleDates.add(startOfFirstDay to endOfFirstDay)
                    }
                    else{
                        scheduleDates.add(start to endOfFirstDay)
                    }


                    //如果最後一天有跨日的情況
                    if((endofTime.isAfter(startBoundary) || endofTime.equals(startBoundary)) &&
                        (endofTime.isBefore(endBoundary))){
                        // 中間日期為起床到睡覺
                        var current = startDate.plusDays(1)
                        //endDate要-1天 這樣他才能成為熬夜的壞小孩
                        while (current.isBefore(endDate.minusDays(1))) {
                            val startOfDay = LocalDateTime.of(current, wake)
                            val endOfDay = LocalDateTime.of(current.plusDays(1), sleep)
                            scheduleDates.add(startOfDay to endOfDay)
                            current = current.plusDays(1)
                        }
                        //最後一天存起床到結束時間
                        val startOfLastDay = LocalDateTime.of(endDate.minusDays(1), wake)
                        scheduleDates.add(startOfLastDay to end)
                    }
                    // 如果最後一天沒有跨日的情況
                    else{
                        // 中間日期為起床到睡覺
                        var current = startDate.plusDays(1)
                        while (current.isBefore(endDate)) {
                            val startOfDay = LocalDateTime.of(current, wake)
                            val endOfDay = LocalDateTime.of(current.plusDays(1), sleep)
                            scheduleDates.add(startOfDay to endOfDay)
                            current = current.plusDays(1)
                        }
                        //最後一天存起床到結束時間
                        val startOfLastDay = LocalDateTime.of(endDate, wake)
                        scheduleDates.add(startOfLastDay to end)
                    }
                }
            }
            // 起床睡覺時間沒有跨日的情況下
            else {
                //如果開始與結束是同一天(包含過午夜十二點的)
                if (startDate.isEqual(endDate) || (startDate.plusDays(1).isEqual(endDate) && endofTime.isBefore(endBoundary))) {
                    val startTime = start.toLocalTime()
                    val endTime = end.toLocalTime()
                    if(startTime.isBefore(wake) && endTime.isAfter(wake) && startTime.isBefore(endBoundary)){
                        val startOfFirstDay = LocalDateTime.of(startDate,wake)
                        scheduleDates.add(startOfFirstDay to end)
                    }
                    else{
                        scheduleDates.add(start to end)
                    }
                }
                else {
                    // 第一天存開始到睡覺時間
                    val endOfFirstDay = LocalDateTime.of(startDate, sleep)
                    val startTime = start.toLocalTime()
                    val endTime = end.toLocalTime()
                    if(startTime.isBefore(wake) && endTime.isAfter(wake) && startTime.isBefore(endBoundary)){
                        val startOfFirstDay = LocalDateTime.of(startDate,wake)
                        scheduleDates.add(startOfFirstDay to endOfFirstDay)
                    }
                    else{
                        scheduleDates.add(start to endOfFirstDay)
                    }

                    //如果最後一天有跨日的情況
                    if((endofTime.isAfter(startBoundary) || endofTime.equals(startBoundary)) &&
                        (endofTime.isBefore(endBoundary))){
                        // 中間日期為起床到睡覺
                        var current = startDate.plusDays(1)
                        //endDate要-1天 這樣他才能成為熬夜的壞小孩
                        while (current.isBefore(endDate.minusDays(1))) {
                            val startOfDay = LocalDateTime.of(current, wake)
                            val endOfDay = LocalDateTime.of(current, sleep)
                            scheduleDates.add(startOfDay to endOfDay)
                            current = current.plusDays(1)
                        }
                        //最後一天存起床到結束時間
                        val startOfLastDay = LocalDateTime.of(endDate.minusDays(1), wake)
                        scheduleDates.add(startOfLastDay to end)
                    }
                    // 如果最後一天沒有跨日的情況
                    else{
                        // 中間日期為起床到睡覺
                        var current = startDate.plusDays(1)
                        while (current.isBefore(endDate)) {
                            val startOfDay = LocalDateTime.of(current, wake)
                            val endOfDay = LocalDateTime.of(current, sleep)
                            scheduleDates.add(startOfDay to endOfDay)
                            current = current.plusDays(1)
                        }
                        //最後一天存起床到結束時間
                        val startOfLastDay = LocalDateTime.of(endDate, wake)
                        scheduleDates.add(startOfLastDay to end)
                    }
                }
            }



            // 印出時間範圍的結果
            scheduleDates.forEach { (startSlot, endSlot) ->
                println("[$startSlot, $endSlot]")
            }

            getEventsForDateRange(startDate, endDate) { events ->
                val availableSlots = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

                for ((startSlot, endSlot) in scheduleDates) {
                    val eventsForDate = events
                        .filter { event ->
                            val eventStart = LocalDateTime.parse(event.startTime, dateTimeFormatter)
                            val eventEnd = LocalDateTime.parse(event.endTime, dateTimeFormatter)
                            eventStart.isBefore(endSlot) && eventEnd.isAfter(startSlot)
                        }
                        .flatMap { event ->
                            val eventStart = LocalDateTime.parse(event.startTime, dateTimeFormatter)
                            val eventEnd = LocalDateTime.parse(event.endTime, dateTimeFormatter)

                            generateSequence(eventStart) { it.plusMinutes(1) }
                                .takeWhile { it.isBefore(eventEnd) }
                                .groupBy { it.toLocalDate() }
                                .map { (date, times) ->
                                    times.first() to times.last()
                                }
                        }

                    // 合併跨日事件
                    val mergedEvents = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
                    var currentStart: LocalDateTime? = null
                    var currentEnd: LocalDateTime? = null

                    for ((eventStart, eventEnd) in eventsForDate) {

                        if (currentStart == null) {
                            currentStart = eventStart
                            currentEnd = eventEnd.plusMinutes(1)

                        } else if (eventStart.isBefore(currentEnd!!.plusMinutes(1))) {
                            // 如果事件開始時間在當前事件結束時間之內，則擴展當前事件的結束時間
                            currentEnd = maxOf(currentEnd!!, eventEnd.plusMinutes(1))
                        } else {
                            // 否則，將當前事件加入到合併事件列表中，並重置
                            mergedEvents.add(currentStart!! to currentEnd!!)
                            currentStart = eventStart
                            currentEnd = eventEnd.plusMinutes(1)
                        }
                    }
                    if (currentStart != null && currentEnd != null) {
                        mergedEvents.add(currentStart to currentEnd)
                    }

                    // 檢查每個合併的事件的 disturb 標誌
                    var currentStartSlot = startSlot
                    for ((eventStart, eventEnd) in mergedEvents) {
                        val eventDisturb = events.find { event ->
                            val start = LocalDateTime.parse(event.startTime, dateTimeFormatter)
                            val end = LocalDateTime.parse(event.endTime, dateTimeFormatter)
                            eventStart == start && eventEnd == end
                        }?.disturb ?: false

                        println("Event Start: $eventStart, Event End: $eventEnd, Disturb: $eventDisturb")

                        if (!eventDisturb) {
                            if (eventStart.isAfter(currentStartSlot)) {
                                availableSlots.add(currentStartSlot to eventStart)
                            }
                            currentStartSlot = eventEnd
                        }
                    }
                    if (currentStartSlot.isBefore(endSlot)) {
                        availableSlots.add(currentStartSlot to endSlot)
                    }
                }
                // 去除重複的時間範圍
                val uniqueSlots = mergeIntervals(
                    availableSlots.map { it.first to it.second }
                        .distinctBy { it.first to it.second }
                        .sortedBy { it.first }
                )

                // 轉換回字符串格式
                callback(uniqueSlots.map { (start, end) ->
                    start.format(outputFormatter) to end.format(outputFormatter)
                })
            }
        }
    }

    //獲取理想時間
    fun filterSlotsByIdealTime(
        availableSlots: List<Pair<LocalDateTime, LocalDateTime>>,
        idealTimeCondition: String,
        callback: (List<Pair<String, String>>) -> Unit
    ) {

        // 定義輸出格式
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")


        val (idealTimeStr, condition) = idealTimeCondition.split("|")
        val idealTimeParsed = LocalTime.parse(idealTimeStr)
        val endBoundary = LocalTime.parse("05:01", timeFormatter) //可能算是同一天的跨日時間點判斷 比如說我們就是覺得凌晨三點還是同一天
        val startBoundary = LocalTime.parse("00:00", timeFormatter)
        println("idealTimeStr:$idealTimeStr")
        println("condition:$condition")


        getWakeSleepTime { wakeTime, sleepTime ->
            val filteredSlots = availableSlots.mapNotNull { (start, end) ->
                val startDate = start.toLocalDate()
                val endDate = end.toLocalDate()
                val startOfTime = start.toLocalTime()

                val wake = LocalTime.parse(wakeTime)
                val sleep = LocalTime.parse(sleepTime)
                val wakeUpTime = startDate.atTime(wake)
                val sleepTime = if((sleep.isAfter(startBoundary) || sleep.equals(startBoundary)) && sleep.isBefore(endBoundary)){
                    if(startDate == endDate){
                        startDate.plusDays(1).atTime(sleep)
                    }else endDate.atTime(sleep)
                }else startDate.atTime(sleep)


                when (condition) {
                    "之前" -> {
                        val endOfIdealSlot =
                            if((idealTimeParsed.isAfter(startBoundary) || idealTimeParsed.equals(startBoundary)) && idealTimeParsed.isBefore(endBoundary)){
                                if(startDate == endDate){
                                    if((startOfTime.isAfter(startBoundary)|| startOfTime.equals(startBoundary)) && startOfTime.isBefore(endBoundary)){
                                        startDate.atTime(idealTimeParsed)
                                    }else startDate.plusDays(1).atTime(idealTimeParsed)
                                }else endDate.atTime(idealTimeParsed)
                            }else if((startOfTime.isAfter(startBoundary)|| startOfTime.equals(startBoundary)) && startOfTime.isBefore(endBoundary)){
                                startDate.minusDays(1).atTime(idealTimeParsed)
                            }else startDate.atTime(idealTimeParsed)

                        println("$endOfIdealSlot")
                        if (start.isBefore(wakeUpTime)) {
                            // 处理事件开始时间早于起床时间的情况
                            if (end.isAfter(start)) {
                                val newEnd = minOf(end, endOfIdealSlot)
                                if (start.isBefore(newEnd)) start to newEnd else null
                            } else null
                        } else if (wakeUpTime.isBefore(endOfIdealSlot) && end.isAfter(wakeUpTime)) {
                            val newStart = maxOf(start, wakeUpTime)
                            val newEnd = if (end.isAfter(sleepTime)) end
                            else minOf(end, endOfIdealSlot)
                            if (newStart.isBefore(newEnd)) newStart to newEnd else null
                        } else null
                    }

                    "之後" -> {
                        val startOfIdealSlot =
                            if((idealTimeParsed.isAfter(startBoundary) || idealTimeParsed.equals(startBoundary)) && idealTimeParsed.isBefore(endBoundary)){
                                if(startDate == endDate){
                                    if((startOfTime.isAfter(startBoundary)|| startOfTime.equals(startBoundary)) && startOfTime.isBefore(endBoundary)){
                                        startDate.atTime(idealTimeParsed)
                                    }else startDate.plusDays(1).atTime(idealTimeParsed)
                                }else endDate.atTime(idealTimeParsed)
                            }else if((startOfTime.isAfter(startBoundary)|| startOfTime.equals(startBoundary)) && startOfTime.isBefore(endBoundary)){
                                startDate.minusDays(1).atTime(idealTimeParsed)
                            }
                            else startDate.atTime(idealTimeParsed)

                        println("$startOfIdealSlot")
                        if (end.isAfter(startOfIdealSlot)) {
                            // 判断事件结束时间是否在理想时间之后
                            val newStart = maxOf(start, startOfIdealSlot)

                            // 如果事件结束时间晚于睡觉时间，则保持事件结束时间
                            val newEnd = if (end.isAfter(sleepTime)) end
                            else minOf(end, sleepTime)

                            if (newStart.isBefore(newEnd)) newStart to newEnd else null
                        } else null
                    }

                    else -> null
                }
            }
            println("availableSlots:$availableSlots")
            println("filteredSlots:$filteredSlots")

            if (filteredSlots.isEmpty()) {
                callback(
                    availableSlots.map { (start, end) ->
                        start.format(outputFormatter) to end.format(outputFormatter)
                    }
                )
            } else {
                callback(
                    filteredSlots.map { (start, end) ->
                        start.format(outputFormatter) to end.format(outputFormatter)
                    }
                )
            }
        }
    }

    //透過標籤編好篩選可用時間
    fun filterSlotsByTagPreferences(
        availableSlots: List<Pair<LocalDateTime, LocalDateTime>>,
        preferences: List<String>,
        callback: (List<Pair<String, String>>) -> Unit
    ) {


        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val preferredSlots = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

        // 根据标签获取所有偏好时间段
        val timeRanges = preferences.map { pref ->
            val (start, end) = when (pref) {
                "上午" -> LocalTime.parse("05:00") to LocalTime.parse("12:00")
                "下午" -> LocalTime.parse("12:00") to LocalTime.parse("18:00")
                "晚上" -> LocalTime.parse("18:00") to LocalTime.parse("05:00")
                else -> LocalTime.parse("00:00") to LocalTime.parse("23:59")
            }
            start to end
        }

        availableSlots.forEach { (start, end) ->
            timeRanges.forEach { (prefStart, prefEnd) ->
                val endBoundary = LocalTime.parse("05:01", timeFormatter)
                val startBoundary = LocalTime.parse("00:00", timeFormatter)

                val startTime = start.toLocalTime()
                val endTime = end.toLocalTime()
                val startDate = start.toLocalDate()
                val endDate = end.toLocalDate()

                val date = if ((startTime.isAfter(startBoundary) || startTime.equals(startBoundary)) && startTime.isBefore(endBoundary)) {
                    startDate.minusDays(1)
                } else startDate

                // 获取偏好时间的起始和结束时间
                val preferredStart = getPreferredStartTime(date, prefStart)
                val preferredEnd = getPreferredEndTime(date, prefEnd, prefStart)

                println("偏好時間 $preferredStart, $preferredEnd")

                when {
                    // 情况1: start 和 end 都在偏好时间段内
                    start.isAfter(preferredStart) && end.isBefore(preferredEnd) -> {
                        preferredSlots.add(start to end)
                    }
                    // 情况2: start 早于偏好时间段，但 end 在偏好时间段内
                    start.isBefore(preferredStart) && end.isAfter(preferredStart) && end.isBefore(preferredEnd) -> {
                        preferredSlots.add(preferredStart to end)
                    }
                    // 情况3: start 在偏好时间段内，但 end 晚于偏好时间段
                    start.isAfter(preferredStart) && start.isBefore(preferredEnd) && end.isAfter(preferredEnd) -> {
                        preferredSlots.add(start to preferredEnd)
                    }
                    // 情况4: start 和 end 都超出了偏好时间段
                    start.isBefore(preferredStart) && end.isAfter(preferredEnd) -> {
                        preferredSlots.add(preferredStart to preferredEnd)
                    }
                }
            }
        }

        val mergeSlots = mergeSlots(preferredSlots)

        val result = if (mergeSlots.isEmpty()) {
            availableSlots.map { (start, end) ->
                start.format(outputFormatter) to end.format(outputFormatter)
            }
        } else {
            mergeSlots.map { (start, end) ->
                start.format(outputFormatter) to end.format(outputFormatter)
            }
        }

        // 使用 callback 返回結果
        callback(result)
    }


    private fun getPreferredStartTime(date: LocalDate, prefStart: LocalTime): LocalDateTime {
        return date.atTime(prefStart)
    }

    private fun getPreferredEndTime(date: LocalDate, prefEnd: LocalTime, prefStart: LocalTime): LocalDateTime {
        // 处理晚上时间段跨日的情况
        val endDate = if (prefEnd.isBefore(prefStart)) date.plusDays(1) else date
        return endDate.atTime(prefEnd)
    }

    //合併重疊時間
    fun mergeSlots(slots: List<Pair<LocalDateTime, LocalDateTime>>): List<Pair<LocalDateTime, LocalDateTime>> {
        if (slots.isEmpty()) return emptyList()

        // 对时间段按开始时间排序
        val sortedSlots = slots.sortedBy { it.first }

        val mergedSlots = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
        var (currentStart, currentEnd) = sortedSlots[0]

        for ((start, end) in sortedSlots.drop(1)) {
            if (start <= currentEnd) {
                // 如果当前时间段与合并中的时间段重叠或相邻，更新结束时间
                if (end > currentEnd) {
                    currentEnd = end
                }
            } else {
                // 否则，将当前合并的时间段添加到列表，并开始新的合并
                mergedSlots.add(currentStart to currentEnd)
                currentStart = start
                currentEnd = end
            }
        }

        // 添加最后一个合并的时间段
        mergedSlots.add(currentStart to currentEnd)

        return mergedSlots
    }


    //去除重複時間的function
    fun mergeIntervals(intervals: List<Pair<LocalDateTime, LocalDateTime>>): List<Pair<LocalDateTime, LocalDateTime>> {
        if (intervals.isEmpty()) return emptyList()

        val mergedIntervals = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
        var currentStart = intervals[0].first
        var currentEnd = intervals[0].second

        for (i in 1 until intervals.size) {
            val (start, end) = intervals[i]
            if (start.isBefore(currentEnd) || start.isEqual(currentEnd)) {
                // Merge overlapping or contiguous intervals
                if (end.isAfter(currentEnd)) {
                    currentEnd = end
                }
            } else {
                // Add the previous interval and start a new one
                mergedIntervals.add(currentStart to currentEnd)
                currentStart = start
                currentEnd = end
            }
        }
        mergedIntervals.add(currentStart to currentEnd) // Add the last interval

        return mergedIntervals
    }

    //計算總共空閒時間
    fun calculateTotalFreeTime(availableSlots: List<Pair<LocalDateTime, LocalDateTime>>): String {
        // 計算總的可用時間（以毫秒為單位）
        val totalMillis = availableSlots.sumOf { (start, end) ->
            Duration.between(start, end).toMillis()
        }

        // 將總的毫秒轉換為 Duration 對象
        val totalDuration = Duration.ofMillis(totalMillis)
        val hours = totalDuration.toHours()
        val minutes = totalDuration.toMinutes() - (hours * 60)

        // 返回格式化的時間字符串
        return "%d:%02d".format(hours, minutes)
    }


    // 計算每日的空閒時間
    fun calculateDailyFreeTime(availableSlots: List<Pair<LocalDateTime, LocalDateTime>>, endTimeBoundary: LocalTime): Map<LocalDate, String> {
        val dailyFreeTime = mutableMapOf<LocalDate, Duration>()
        val nextDayStart = LocalTime.MIN

        availableSlots.forEach { (start, end) ->
            var currentStart = start

            println("Start processing slot: $currentStart to $end")

            // 處理每個時間段，按天分割
            while (currentStart.toLocalDate() != end.toLocalDate()) {

                val endOfDay = LocalDateTime.of(currentStart.toLocalDate(), LocalTime.MAX)
                val dayEnd = if (endOfDay.isBefore(end)) endOfDay else end

                // 計算當天的可用時間
                if (currentStart.isBefore(dayEnd)) {
                    val duration = Duration.between(currentStart, dayEnd)
                    dailyFreeTime[currentStart.toLocalDate()] = dailyFreeTime.getOrDefault(currentStart.toLocalDate(), Duration.ZERO) + duration
                }

                // 移動到下一天
                currentStart = LocalDateTime.of(currentStart.toLocalDate().plusDays(1), nextDayStart)
            }

            // 處理最後一天
            val endOfLastDay = LocalDateTime.of(end.toLocalDate(), end.toLocalTime())
            if (currentStart.toLocalDate() == end.toLocalDate()) {
                val duration = Duration.between(currentStart, endOfLastDay)
                // 如果最後一天的空閒時間在早上五點之前，加到前一天
                if (endOfLastDay.toLocalTime().isBefore(LocalTime.of(5, 0))) {
                    val previousDay = currentStart.toLocalDate().minusDays(1)
                    dailyFreeTime[previousDay] = dailyFreeTime.getOrDefault(previousDay, Duration.ZERO) + duration
                }
                // 如果不在早上五點之前，正常處理最後一天的空閒時間
                else {
                    dailyFreeTime[end.toLocalDate()] = dailyFreeTime.getOrDefault(end.toLocalDate(), Duration.ZERO) + duration
                }
            }
            else{

            }
        }

        // 格式化結果
        return dailyFreeTime.mapValues { (date, duration) ->
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            "%d:%02d".format(hours, minutes)
        }
    }



    private fun getEventsForDateRange(startDate: LocalDate, endDate: LocalDate, callback: (List<Event>) -> Unit) {
        val memberId = auth.currentUser?.uid ?: return callback(emptyList())
        Log.d("free", memberId)
        val eventRef = database.getReference("members").child(memberId).child("events")
        val eventsList = mutableListOf<Event>()

        eventRef.get().addOnSuccessListener { snapshot ->
            try {
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
                        val duration = eventMap["duration"] as? String ?: ""
                        val idealTime = eventMap["idealTime"] as? String ?: ""
                        val shortestTime = eventMap["shortestTime"] as? String ?: ""
                        val longestTime = eventMap["longestTime"] as? String ?: ""
                        val dailyRepeat = eventMap["dailyRepeat"] as? Boolean ?: false
                        val disturb = eventMap["disturb"] as? Boolean ?: false
                        val description = eventMap["description"] as? String ?: ""

                        // 解析事件的 startTime 和 endTime
                        val eventStartDate = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
                        val eventEndDate = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()

                        // 檢查事件是否在指定日期範圍內
                        if ((eventStartDate.isBefore(endDate) || eventStartDate.isEqual(endDate)) &&
                            (eventEndDate.isAfter(startDate) || eventEndDate.isEqual(startDate))) {
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
                                duration = duration,
                                idealTime = idealTime,
                                shortestTime = shortestTime,
                                longestTime = longestTime,
                                dailyRepeat = dailyRepeat,
                                disturb = disturb,
                                description = description
                            )
                            eventsList.add(event)
                        }
                    }
                }
                callback(eventsList)
            } catch (e: Exception) {
                // 處理錯誤情況
                e.printStackTrace()
                callback(emptyList())
            }
        }.addOnFailureListener {
            // 處理錯誤情況
            callback(emptyList())
        }
    }

}