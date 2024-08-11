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

    fun getFreeTime(
        startTime: String,
        endTime: String,
        wakeTime: String,
        sleepTime: String,
    ): List<Pair<LocalDateTime, LocalDateTime>> {
        // 定義時間格式
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // 解析時間字符串
        val start = LocalDateTime.parse(startTime, dateTimeFormatter)
        val end = LocalDateTime.parse(endTime, dateTimeFormatter)
        val wake = LocalTime.parse(wakeTime, timeFormatter)
        val sleep = LocalTime.parse(sleepTime, timeFormatter)

        // 計算日期範圍
        val startDate = start.toLocalDate()
        val endDate = end.toLocalDate()

        val scheduleDates = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

        if (startDate.isEqual(endDate)) {
            // 當 startTime 和 endTime 是同一天
            scheduleDates.add(start to end)
        } else {
            // 當 startTime 和 endTime 是不同天

            // 處理 startTime 那一天
            val endOfFirstDay = LocalDateTime.of(startDate, sleep)
            scheduleDates.add(start to endOfFirstDay)

            // 處理 endTime 那一天
            val startOfLastDay = LocalDateTime.of(endDate, wake)
            scheduleDates.add(startOfLastDay to end)

            // 處理中間的日期
            var current = startDate.plusDays(1)
            while (current.isBefore(endDate)) {
                val startOfDay = LocalDateTime.of(current, wake)
                val endOfDay = LocalDateTime.of(current, sleep)
                scheduleDates.add(startOfDay to endOfDay)
                current = current.plusDays(1)
            }
        }

        return scheduleDates
    }


    //抓錯誤
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
    }

    fun getFreeTime(
        startTime: String,
        endTime: String,
        wakeTime: String,
        sleepTime: String,
        events: List<Event>
    ): List<Pair<String, String>> {
        // 定義時間格式
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // 解析時間字符串
        val start = LocalDateTime.parse(startTime, dateTimeFormatter)
        val end = LocalDateTime.parse(endTime, dateTimeFormatter)
        val endofTime = end.toLocalTime() //單純存結束時間的時分
        val endBoundary = LocalTime.parse("05:01",timeFormatter) //可能算是同一天的跨日時間點判斷 比如說我們就是覺得凌晨三點還是同一天
        val startBoundary = LocalTime.parse("00:00",timeFormatter) //如果是連續很多天的事件 同上拿來判斷用的
        val wake = LocalTime.parse(wakeTime, timeFormatter)
        val sleep = LocalTime.parse(sleepTime, timeFormatter)

        // 定義輸出格式
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // 計算日期範圍
        val startDate = start.toLocalDate()
        val endDate = end.toLocalDate()

        //存要檢查的時間範圍
        val scheduleDates = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

        // 如果睡覺時間早於起床時間(解決跨日問題)
        if (sleep.isBefore(wake)) {
            //如果事件的開始時間是同一天(包含過午夜十二點的)
            if (startDate.isEqual(endDate) || (startDate.plusDays(1).isEqual(endDate) && endofTime.isBefore(endBoundary))) {
                scheduleDates.add(start to end)
            }
            else {
                // 第一天存開始到睡覺時間
                val endOfFirstDay = LocalDateTime.of(startDate.plusDays(1), sleep)
                scheduleDates.add(start to endOfFirstDay)



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
                scheduleDates.add(start to end)
            }
            else {
                // 第一天存開始到睡覺時間
                val endOfFirstDay = LocalDateTime.of(startDate, sleep)
                scheduleDates.add(start to endOfFirstDay)



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

        // 儲存空檔時間範圍
        val availableSlots = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()

        for ((startSlot, endSlot) in scheduleDates) {
            // 先過濾出與當前時間段重疊的事件
            val eventsForDate = events
                .filter { event ->
                    val eventStart = LocalDateTime.parse(event.startTime, dateTimeFormatter)
                    val eventEnd = LocalDateTime.parse(event.endTime, dateTimeFormatter)
                    eventStart.isBefore(endSlot) && eventEnd.isAfter(startSlot)
                }
                .flatMap { event ->
                    val eventStart = LocalDateTime.parse(event.startTime, dateTimeFormatter)
                    val eventEnd = LocalDateTime.parse(event.endTime, dateTimeFormatter)

                    // 生成事件在當前時間段的時間範圍
                    generateSequence(eventStart.coerceAtLeast(startSlot)) { it.plusMinutes(1) }
                        .takeWhile { it.isBefore(eventEnd.coerceAtMost(endSlot)) }
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
        return uniqueSlots.map { (start, end) ->
            start.format(outputFormatter) to end.format(outputFormatter)
        }
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

}