package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.data.Event
import java.util.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import androidx.compose.ui.res.colorResource as colorResource1


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DailyCalendarScreen(
    navController: NavController,
    evm: EventViewModel,
    mvm: MemberViewModel,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(date) }  // 設置為 LocalDate.now() 以確保能夠顯示當前日期
    var selectedHour by remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf(LocalDate.now()) }  // 這個值應該用於顯示當前月份的視圖
    var expanded by remember { mutableStateOf(false) } // 控制下拉選單的狀態
    val events by evm.events.observeAsState(emptyList())
    val eventPositions = remember { mutableMapOf<String, MutableMap<String, Float>>() }

    LaunchedEffect(selectedDate, events) {
        // 確保事件和排版位置是同步的
        evm.getEventsForDate(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        eventPositions.clear()
    }

    Scaffold(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.change),
                            contentDescription = "More Options",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                navController.navigate(DestinationScreen.MonthCalendar.route)
                            },
                            text = {
                                Text("月行事曆")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                navController.navigate(DestinationScreen.Main.route)
                            },
                            text = {
                                Text("個人資料")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                navController.navigate(DestinationScreen.Classification.route)
                            },
                            text = {
                                Text("標籤分類")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                navController.navigate(DestinationScreen.TimeReport.route)
                            },
                            text = {
                                Text("行程分析")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                mvm.logout()
                            },
                            text = {
                                Text("登出")
                            }
                        )
                    }
                }

                IconButton(onClick = { currentDate.value = currentDate.value.minusWeeks(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Week",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    "${currentDate.value.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentDate.value.year}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                IconButton(onClick = { currentDate.value = currentDate.value.plusWeeks(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Week",
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Event",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DayOfWeek.values().forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                val startOfWeek = currentDate.value.with(DayOfWeek.MONDAY) //计算当前日期所在周的开始日期（星期一）
                (0..6).forEach { i ->
                    val date = startOfWeek.plusDays(i.toLong())
                    DayCell(
                        day = date,
                        isToday = date == LocalDate.now(),
                        isSelected = selectedDate == date,
                        onDateSelected = {
                            selectedDate = it
                            // 更新 selectedDate 以顯示該日期的時間軸
                            Log.d("DailyCalendarScreen", "Selected date: $selectedDate")  // 日誌輸出選擇的日期
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // 這裡應該只有設定 showDialog
                                // showDialog = true // 可以保留，這裡的點擊事件已經被上面的 DateCell 處理了
                            },
                            onLongPress = { offset ->
                                // 根據點擊的位置計算選擇的時間
                                val hour = (offset.y / 60).toInt()
                                selectedHour = hour
                                    .toString()
                                    .padStart(2, '0')
                                showDialog = true
                            }
                        )
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    items(24) { hour ->
                        val hourString = hour.toString().padStart(2, '0')
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            // 點擊小時格子時，選擇該小時
                                            selectedHour = hourString
                                            showDialog = true
                                        }
                                    )
                                }
                        ) {
                            DailyRow(hourString,evm ,events,eventPositions, selectedDate.toString())
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        UserInputDialog(
            selectedDate = selectedDate,  // 使用選擇的日期來顯示對話框
            evm = evm,
            onDismiss = { showDialog = false },
            selectedHour = selectedHour
        )
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyRow(
    hour: String,
    evm: EventViewModel,
    events: List<Event>,
    eventPositions: MutableMap<String, MutableMap<String, Float>>,
    currentDay: String
) {
    val hourInt = hour.toInt()
    val eventWidth = 80.dp // 預設寬度，根據需要調整
    val eventSpacing = 4.dp // 事件之間的間距

    // 日期格式化器
    val eventFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val currentDayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // 清理並解析 event.date 字符串
    fun parseEventDate(dateString: String): LocalDateTime {
        return LocalDateTime.parse(dateString.trim(), eventFormatter)
    }

    // 清理並解析 currentDay 字符串
    fun parseCurrentDay(dateString: String): LocalDate {
        return LocalDate.parse(dateString.trim(), currentDayFormatter)
    }

    val currentDayFormatted = parseCurrentDay(currentDay)

    // 過濾出當天的事件
    val eventsForDay = remember(events) {
        events.filter { event ->
            val eventStartTime = parseEventDate(event.startTime)
            val eventEndTime = parseEventDate(event.endTime)
            val eventStartDate = eventStartTime.toLocalDate()
            val eventEndDate = eventEndTime.toLocalDate()
            (eventStartDate <= currentDayFormatted && eventEndDate >= currentDayFormatted)
        }
    }

    // 過濾出當前小時的事件
    val eventsForHour = remember(eventsForDay) {
        eventsForDay.filter { event ->
            val eventStartTime = parseEventDate(event.startTime)
            val eventEndTime = parseEventDate(event.endTime)
            val eventStartDate = eventStartTime.toLocalDate()
            val eventEndDate = eventEndTime.toLocalDate()

            // 確保事件在當天，並檢查事件是否在當前小時內
            val isForHour = if (eventStartDate.isEqual(currentDayFormatted) || eventEndDate.isEqual(currentDayFormatted)) {
                (eventStartTime.hour <= hourInt && eventEndTime.hour >= hourInt) ||
                        (eventStartDate.isBefore(currentDayFormatted) && eventEndDate.isEqual(currentDayFormatted) && eventEndTime.hour >= hourInt) ||
                        (eventStartDate.isEqual(currentDayFormatted) && eventEndDate.isAfter(currentDayFormatted) && eventStartTime.hour <= hourInt)
            } else {
                // 處理跨日事件
                val eventSpanDays = ChronoUnit.DAYS.between(eventStartDate, eventEndDate).toInt()
                if (eventSpanDays > 1) {
                    for (day in 1 until eventSpanDays) {
                        if (eventStartDate.plusDays(day.toLong()).isEqual(currentDayFormatted)) {
                            return@filter true
                        }
                    }
                }
                false
            }
            isForHour
        }.sortedBy { event ->
            parseEventDate(event.startTime)
        }
    }

    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    // 用於追蹤已顯示過名稱的事件
    val shownEvents = rememberSaveable { mutableSetOf<String>() }

    // Define a function to compute position outside of the Composable
    val eventWidthPx = with(LocalDensity.current) { eventWidth.toPx() }
    val eventSpacingPx = with(LocalDensity.current) { eventSpacing.toPx() }

    fun computeEventOffset(event: Event, eventsForHour: List<Event>): Float {
        val eventKey = event.uid

        // If position is already computed for any hour, return it
        eventPositions.values.forEach { positionsForHour ->
            positionsForHour[eventKey]?.let { return it }
        }

        // Compute new position if not found
        val position = run {
            var newPosition = 0f
            var isPositionFound: Boolean

            do {
                isPositionFound = true
                for (e in eventsForHour) {
                    if (event != e && eventPositions[hour]?.get(e.uid) == newPosition) {
                        isPositionFound = false
                        newPosition += (eventWidthPx + eventSpacingPx)
                        break
                    }
                }
            } while (!isPositionFound)

            newPosition
        }

        // Update position for all hours the event spans
        val eventStartTime = parseEventDate(event.startTime)
        val eventEndTime = parseEventDate(event.endTime)

        // Loop through each hour the event spans
        for (h in eventStartTime.hour..eventEndTime.hour) {
            val hourKey = h.toString().padStart(2, '0')
            val positionsForHour = eventPositions.getOrPut(hourKey) { mutableMapOf() }
            positionsForHour[eventKey] = position
        }

        return position
    }

    // UI
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$hour:00",
            modifier = Modifier
                .width(60.dp)
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        HorizontalDivider(modifier = Modifier.width(1.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .background(Color.White)
        ) {
            eventsForHour.forEach { event ->
                val eventStartTime = parseEventDate(event.startTime)
                val eventEndTime = parseEventDate(event.endTime)

                val eventDurationMinutes = ChronoUnit.MINUTES.between(eventStartTime, eventEndTime)

                // 計算事件在當前小時中的相對開始位置
                val startFraction = if (eventStartTime.hour == hourInt) {
                    eventStartTime.minute.toFloat() / 60
                } else {
                    0f
                }

                val isEventAtMidnight = {
                    val startTime = eventStartTime.toLocalTime()
                    val endTime = eventEndTime.toLocalTime()

                    // Check if the event starts before midnight and ends after midnight
                    (eventStartTime.toLocalDate().isBefore(eventEndTime.toLocalDate()) ||
                            (eventStartTime.toLocalDate().isEqual(eventEndTime.toLocalDate()) && endTime.isBefore(startTime))) ||
                            (startTime == LocalTime.MIDNIGHT || endTime == LocalTime.MIDNIGHT)
                }

                val heightFraction = when {
                    eventStartTime.hour == hourInt && eventEndTime.hour == hourInt -> {
                        // Event starts and ends within this hour
                        (eventDurationMinutes.toFloat() / 60).coerceIn(0.1f, 1.0f)
                    }
                    eventStartTime.hour == hourInt && eventEndTime.hour > hourInt -> {
                        // Event starts in this hour but ends in the next hour
                        val minutesInCurrentHour = ChronoUnit.MINUTES.between(eventStartTime.toLocalTime(), LocalTime.of(hourInt + 1, 0))
                        (minutesInCurrentHour.toFloat() / 60).coerceIn(0.1f, 1.0f)
                    }
                    eventEndTime.hour == hourInt -> {
                        // Event ends in this hour
                        (eventEndTime.minute.toFloat() / 60).coerceIn(0.1f, 1.0f)
                    }
                    else -> {
                        // Event covers the entire hour
                        1.0f
                    }
                }

                // Retrieve horizontal offset for the event
                val offsetPx = computeEventOffset(event, eventsForHour)
                var uid = event.uid
                Log.d("location", "$uid,$offsetPx")

                Box(
                    modifier = Modifier
                        .offset(
                            x = with(LocalDensity.current) { offsetPx.toDp() },
                            y = with(LocalDensity.current) { (startFraction * 60.dp.toPx()).toDp() })
                        .width(eventWidth)
                        .fillMaxHeight(heightFraction)
                        .background(Color.Blue)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { selectedEvent = event }
                ) {
                    if (eventStartTime.toLocalDate() == currentDayFormatted &&
                        (eventStartTime.hour == hourInt)
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(4.dp)
                        )
                        shownEvents.add(event.uid) // 標記該事件名稱已顯示
                    } else if (hourInt == 0 && isEventAtMidnight()) {
                        // Check if the event spans midnight and it's the next day at 00:00
                        Text(
                            text = event.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(4.dp)
                        )
                        shownEvents.add(event.uid) // 標記該事件名稱已顯示
                    }
                }
            }
        }
    }

    selectedEvent?.let { event ->
        EventDetailDialog(event = event, evm = evm, "daily", onDismiss = { selectedEvent = null })
    }
}




//事件視窗
@Composable
fun EventDetailDialog(event: Event, evm: EventViewModel, temp: String, onDismiss: () -> Unit, currentMonth: LocalDate? = null) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showEditOptionsDialog by remember { mutableStateOf(false) }
    val updatedEvent = remember { mutableStateOf(event) }
    var showDeleteOptionsDialog by remember { mutableStateOf(false) }
    var editAll by remember { mutableStateOf(false) }

    // 觀察 LiveData 中的 events 變化
    val events by evm.events.observeAsState(emptyList())

    // 監控事件列表變化，當事件列表更新時，更新 updatedEvent 的值
    LaunchedEffect(events) {
        val check = events.find { it.repeatType != "無" } != null
        if(check){
            updatedEvent.value = events.find { it.repeatGroupId == event.repeatGroupId } ?: event
        }
        else{
            updatedEvent.value = events.find { it.uid == event.uid } ?: event
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("事件詳情", fontWeight = FontWeight.Bold, color = Color.Black)
                    IconButton(onClick = {
                        if(event.repeatType != "無"){
                            showEditOptionsDialog = true
                        }
                        else{
                            editAll = false
                            showEditDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "編輯", tint = Color.Blue)
                    }
                    IconButton(onClick = {
                        if (event.repeatType != "無") {
                            showDeleteOptionsDialog = true
                        } else {
                            evm.deleteEventFromFirebase(event, temp, deleteAll = false) {
                                showDeleteOptionsDialog = false
                                onDismiss() // 成功刪除後關閉視窗
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "刪除", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("事件名稱: ${updatedEvent.value.name}", color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Text("標籤: ${updatedEvent.value.tags}", color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Text("開始時間: ${updatedEvent.value.startTime}", color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Text("結束時間: ${updatedEvent.value.endTime}", color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Text("備註: ${updatedEvent.value.description}", color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("關閉")
                    }
                }
            }
        }
    }

    if (showEditOptionsDialog) {
        Dialog(onDismissRequest = { showEditOptionsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("編輯重複事件", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("這是一個重複事件。您要修改所有重複項目，還是僅修改此項目？")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            editAll = false
                            showEditDialog = true
                            showEditOptionsDialog = false
                        }) {
                            Text("僅修改此項")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            // 修改所有重複項目的邏輯
                            editAll = true
                            showEditDialog = true
                            showEditOptionsDialog = false
                        }) {
                            Text("修改所有")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showEditOptionsDialog = false
                        }) {
                            Text("取消")
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val selectedDateTime = LocalDateTime.parse(updatedEvent.value.startTime, formatter)

        UserInputDialog(
            selectedDate = selectedDateTime.toLocalDate(),
            evm = evm,
            onDismiss = { showEditDialog = false },
            selectedHour = updatedEvent.value.startTime,
            event = updatedEvent.value,
            currentMonth = currentMonth,
            editAll = editAll
        )
    }

    if (showDeleteOptionsDialog) {
        Dialog(onDismissRequest = { showDeleteOptionsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("刪除重複事件", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("這是一個重複事件。您要刪除所有重複項目，還是僅刪除此項目？")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            evm.deleteEventFromFirebase(event, temp, deleteAll = true) {
                                showDeleteOptionsDialog = false
                                onDismiss()
                            }
                        }) {
                            Text("刪除所有")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            evm.deleteEventFromFirebase(event, temp, deleteAll = false) {
                                showDeleteOptionsDialog = false
                                onDismiss()
                            }
                        }) {
                            Text("僅刪除此項")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            showDeleteOptionsDialog = false
                        }) {
                            Text("取消")
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    day: LocalDate,
    isToday: Boolean,
    isSelected: Boolean = false,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor = when {
        isSelected -> colorResource1(id = R.color.light_blue)
        else -> Color.Transparent
    }
    val textColor = if (isToday) Color.Red else Color.Black

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                Log.d("DayCell", "Selected date: $day")  // 日誌輸出選擇的日期
                onDateSelected(day)  // 傳遞選擇的日期
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            color = textColor
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputDialog(
    selectedDate: LocalDate, evm: EventViewModel, onDismiss: () -> Unit,
    selectedHour: String, event: Event? = null,
    currentMonth: LocalDate? = null,
    editAll: Boolean?= false
) {
    val initialStartHour = if (selectedHour.isNotBlank()) {
        if (selectedHour.length == 2) "$selectedHour:00" else selectedHour
    } else {
        ""
    }
    val initialStartLocalTime = try {
        LocalTime.parse(initialStartHour)
    } catch (e: DateTimeParseException) {
        LocalTime.of(0, 0)  // 如果無法解析 selectedHour，則設置為默認時間 "00:00"
    }

    // 如果是編輯狀態，設置初始值
    val initialName = event?.name ?: ""
    val initialDescription = event?.description ?: ""
    val initialTags = event?.tags ?: ""
    val initialAlarmTime = event?.alarmTime ?: "1天前"
    val initialStartTime = event?.startTime ?: ""
    val initialEndTimeEvent = event?.endTime ?: ""
    val initialRepeatType = event?.repeatType ?: "無"
    val initialRepeatEndDate = event?.repeatEndDate ?: ""
    val initialDuration = event?.duration ?: ""
    val initialIdealTime = event?.idealTime ?: ""
    val initialShortestTime = event?.shortestTime ?: ""
    val initialLongestTime = event?.longestTime ?: ""
    val initialDailyRepeat = event?.dailyRepeat ?: false
    val initialDisturb = event?.disturb ?: false
    val initialAutoSchedule = initialDuration != ""
    val initialIsSplittable = initialShortestTime != "" || initialLongestTime != ""

    var name by remember { mutableStateOf(initialName) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTimeEvent) }
    var startDateTime by remember {
        mutableStateOf(
            LocalDateTime.of(
                selectedDate,
                initialStartLocalTime
            )
        )
    }
    var endDateTime by remember {
        mutableStateOf(
            LocalDateTime.of(
                selectedDate,
                initialStartLocalTime.plusHours(1)
            )
        )
    }
    val selectedDay by remember { mutableStateOf(selectedDate) }
    var description by remember { mutableStateOf(initialDescription) }
    var alarmTime by remember { mutableStateOf(initialAlarmTime) }
    var showStartTimeDialog by remember { mutableStateOf(false) }
    var showEndTimeDialog by remember { mutableStateOf(false) }
    var showAlarmTimeDialog by remember { mutableStateOf(false) }
    var tags by remember { mutableStateOf(initialTags) }  // 當前標籤
    var showTagMenu by remember { mutableStateOf(false) }  // 控制標籤選單的顯示
    var autoSchedule by remember { mutableStateOf(initialAutoSchedule) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    var repeatEndDate by remember { mutableStateOf(initialRepeatEndDate) }
    var repeatType by remember { mutableStateOf(initialRepeatType) }
    val showDialog = mutableStateOf(false)
    val errorMessage = mutableStateOf("")


    var duration by remember { mutableStateOf(initialDuration) }//所需時間
    var idealTime by remember { mutableStateOf(initialIdealTime) }//理想時間
    var isSplittable by remember { mutableStateOf(initialIsSplittable) }//能否分割
    var disturb by remember { mutableStateOf(initialDisturb) }//能否被干擾
    var dailyRepeat by remember { mutableStateOf(initialDailyRepeat) }//自動排程裡的每天重複
    var showSplitDialog by remember { mutableStateOf(false) }//至少時間顯示畫面
    var showSplitDialog1 by remember { mutableStateOf(false) }//至多時間顯示畫面
    var shortestTime by remember { mutableStateOf(initialShortestTime) }//至少用
    var longestTime by remember { mutableStateOf(initialLongestTime) }//至多用
    var checkAuto by remember {mutableStateOf(false)}

    val (timePart, optionPart) = parseIdealTimeString(idealTime)
    var selectedOption by remember { mutableStateOf(optionPart) }


    Log.d("date", "$selectedDay")

    // 回調方法
    fun onStartTimeSelected(dateTime: LocalDateTime) {
        startDateTime = dateTime
        startTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        endDateTime = dateTime.plusHours(1)
        endTime = endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    fun onEndTimeSelected(dateTime: LocalDateTime) {
        endDateTime = dateTime
        endTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 設定事件名稱
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("事件名稱", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 設定開始時間
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("開始時間:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showStartTimeDialog = true },
                        colors = ButtonDefaults.buttonColors(colorResource1(id = R.color.light_blue))
                    ) {
                        Text(startTime.ifBlank { "選擇開始時間" }, color = Color.White)
                    }
                    if (showStartTimeDialog) {
                        DateTimePickerDialog(
                            initialDateTime = startDateTime,
                            onDateTimeSelected = { dateTime ->
                                onStartTimeSelected(dateTime)
                                showStartTimeDialog = false
                            },
                            onDismissRequest = { showStartTimeDialog = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 設定結束時間
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("結束時間:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showEndTimeDialog = true },
                        colors = ButtonDefaults.buttonColors(colorResource1(id = R.color.light_blue))
                    ) {
                        Text(endTime.ifBlank { "選擇結束時間" }, color = Color.White)
                    }
                    if (showEndTimeDialog) {
                        DateTimePickerDialog(
                            initialDateTime = endDateTime,
                            onDateTimeSelected = { dateTime ->
                                onEndTimeSelected(dateTime)
                                showEndTimeDialog = false
                            },
                            onDismissRequest = { showEndTimeDialog = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 標籤選擇
                TagsDropdownMenu(
                    currentTag = tags,
                    onTagSelected = { selectedTag ->
                        tags = selectedTag
                    },
                    onDismiss = { showTagMenu = false }
                )

                Spacer(modifier = Modifier.height(8.dp))

                var showAutoScheduleDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                )
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,  // 将按钮和开关分开放置在Row的两边
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        //自動排程
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("自動排程:", color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = autoSchedule,
                                onCheckedChange = {
                                    autoSchedule = it
                                    if (it) showAutoScheduleDialog = true // 當開關為打開時打開對話框
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorResource1(id = R.color.light_blue),
                                    uncheckedThumbColor = Color.Gray
                                )
                            )

                            if (showAutoScheduleDialog) AlertDialog(
                                onDismissRequest = { showAutoScheduleDialog = false },
                                confirmButton = {
                                    Button(onClick = {
                                        if (autoSchedule && (duration == "00:00")) {
                                            errorMessage.value = "所需時間不可為0"
                                            showDialog.value = true
                                            return@Button
                                        }

                                        if (shortestTime != "" && longestTime != "") {
                                            // 將時間字串轉換為分鐘數
                                            val minMinutes = parseTimeToMinutes(shortestTime)
                                            val maxMinutes = parseTimeToMinutes(longestTime)

                                            // 檢查最少時間是否大於最多時間
                                            if (minMinutes > maxMinutes) {
                                                errorMessage.value = "分割的至少時間不可大於最多時間"
                                                showDialog.value = true
                                                return@Button
                                            }
                                        }
                                        showAutoScheduleDialog = false
                                    })
                                    {
                                        Text("確認")
                                        autoSchedule = true
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = { showAutoScheduleDialog = false }) {
                                        Text("取消")
                                        autoSchedule = false
                                    }
                                },
                                text = {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val (initialHour, initialMinute) = parseDurationString(duration)

                                        val dur = rememberTimePickerState(initialHour, initialMinute, true)
                                        // 所需時間
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "所需時間",
                                                color = Color.Black,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TimeInput(
                                                state = dur,
                                                colors = TimePickerDefaults.colors(
                                                    timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                                                    timeSelectorSelectedContentColor = Color.Black,
                                                    timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                                                    timeSelectorUnselectedContentColor = Color.Black
                                                ),
                                                modifier = Modifier
                                                    .size(180.dp, 70.dp)
                                                    .align(Alignment.CenterVertically)
                                            )
                                        }
                                        duration = formatDuration(duration = dur)

                                        Spacer(modifier = Modifier.height(8.dp))


                                        val hours = (0..23).map { String.format("%02d:00", it) } + "無"
                                        val selectedHour = remember { mutableStateOf("無") }  // 預設選項為"無"
                                        var showDialogIdealTime by remember { mutableStateOf(false) }


                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "理想時間",
                                                color = Color.Black,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = { showDialogIdealTime = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent, // 背景颜色设置为透明
                                                    contentColor = Color.Black
                                                ),
                                                border = BorderStroke(2.dp, colorResource(id = R.color.dark_blue)) // 设置边框
                                            ) {
                                                Text(text = selectedHour.value,fontSize = 20.sp)
                                            }

                                            if (showDialogIdealTime) {
                                                Dialog(onDismissRequest = { showDialogIdealTime = false }) {
                                                    Surface(
                                                        shape = MaterialTheme.shapes.medium,
                                                        color = Color.White,
                                                        modifier = Modifier
                                                            .padding(6.dp)

                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .padding(16.dp)
                                                                .fillMaxWidth()
                                                        ) {
                                                            LazyVerticalGrid(
                                                                columns = GridCells.Fixed(4),
                                                                contentPadding = PaddingValues(10.dp),

                                                                modifier = Modifier
                                                                    .height(320.dp)
                                                                // 控制对话框高度
                                                            ) {
                                                                items(hours.size) { index ->  // 传入列表大小
                                                                    val hour = hours[index]   // 获取当前小时字符串
                                                                    val isSelected = hour == selectedHour.value
                                                                    val itemsInRow = 4 // 每行的项目数
                                                                    val isLastRow = index / itemsInRow == hours.size / itemsInRow
                                                                    val modifier = if (isLastRow) {
                                                                        Modifier
                                                                            .padding(4.dp)
                                                                            .weight(1f) // 在最后一行的项目间均匀分布空间
                                                                            .fillMaxWidth()
                                                                    } else {
                                                                        Modifier
                                                                            .padding(4.dp)
                                                                            .fillMaxWidth()
                                                                    }
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .padding(4.dp)
                                                                            .weight(1f)
                                                                            .border(
                                                                                BorderStroke(
                                                                                    1.dp,
                                                                                    if (isSelected) Color.Red else Color.Gray
                                                                                )
                                                                            )
                                                                            .background(Color.Transparent)
                                                                            .clickable {
                                                                                selectedHour.value = hour
                                                                            }
                                                                            .padding(6.dp), // 增加内边距
                                                                        //.fillMaxWidth()
                                                                        //.align(Alignment.CenterHorizontally), // 水平居中对齐
                                                                        contentAlignment = Alignment.Center // 内容居中对齐
                                                                    ) {
                                                                        Text(
                                                                            text = hour,
                                                                            color = Color.Black,
                                                                            fontSize = 14.sp, // 设置字体大小
                                                                            textAlign = TextAlign.Center,
                                                                            modifier = Modifier.fillMaxSize()
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Button(
                                                onClick = {
                                                    selectedOption = if (selectedOption == "之前") "之後"
                                                    else "之前"
                                                }
                                            ) {
                                                Text(text = selectedOption)
                                            }
                                        }
                                        // 将理想时间格式化为 "HH:mm|之前" 或 "HH:mm|之後"
                                        // 如果选择了“无”，返回空字符串 ""
                                        idealTime = if (selectedHour.value == "無") {
                                            ""
                                        } else {
                                            "${selectedHour.value}|${selectedOption}"
                                        }
                                }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        //能否分割
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("能否分割:", color = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Checkbox(
                                                checked = isSplittable,
                                                onCheckedChange = { isSplittable = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = colorResource1(id = R.color.light_blue),
                                                    uncheckedColor = Color.Gray
                                                )
                                            )
                                        }
                                        if (isSplittable) {
                                            Text("選擇至少分割時間:", color = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { showSplitDialog = true },
                                                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                                            ) {
                                                if (showSplitDialog) {
                                                    AlertDialog(
                                                        onDismissRequest = { showSplitDialog = false },
                                                        confirmButton = {
                                                            Button(
                                                                onClick = { showSplitDialog = false }
                                                            ) {
                                                                Text("確認")
                                                            }
                                                        },
                                                        dismissButton = {
                                                            Button(onClick = { showSplitDialog = false }) {
                                                                Text("取消")
                                                            }
                                                        },
                                                        text = {
                                                            LazyColumn {
                                                                items(
                                                                    listOf( "30min",  "1hr",  "1hr30min",  "2hr"
                                                                    )
                                                                )
                                                                { interval ->
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clickable {
                                                                                shortestTime = interval
                                                                                showSplitDialog = false
                                                                            }
                                                                            .padding(8.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        RadioButton(
                                                                            selected = shortestTime == interval,
                                                                            onClick = {
                                                                                shortestTime = interval
                                                                                showSplitDialog = false
                                                                            }
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text(text = interval)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                                shortestTime?.let {
                                                    Text(
                                                        "最少: $it",
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(top = 16.dp)
                                                    )
                                                }
                                            }
                                            Text("選擇最多分割時間:", color = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { showSplitDialog1 = true },
                                                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                                            ) {
                                                if (showSplitDialog1) {
                                                    AlertDialog(
                                                        onDismissRequest = { showSplitDialog1 = false },
                                                        confirmButton = {
                                                            Button(
                                                                onClick = { showSplitDialog1 = false }
                                                            ) {
                                                                Text("確認")
                                                            }
                                                        },
                                                        dismissButton = {
                                                            Button(onClick = { showSplitDialog1 = false }) {
                                                                Text("取消")
                                                            }
                                                        },
                                                        text = {
                                                            LazyColumn {
                                                                items(
                                                                    listOf("30min", "1hr", "1hr30min", "2hr", "2hr30min","3hr","3hr30min","4hr")
                                                                )
                                                                { interval ->
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clickable {
                                                                                longestTime = interval
                                                                                showSplitDialog1 = false
                                                                            }
                                                                            .padding(8.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        RadioButton(
                                                                            selected = longestTime == interval,
                                                                            onClick = {
                                                                                longestTime = interval
                                                                                showSplitDialog1 = false
                                                                            }
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text(text = interval)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                                longestTime?.let {
                                                    Text(
                                                        "最多: $it",
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(top = 16.dp)
                                                    )
                                                }

                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        //是否要每天重複排
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("是否要每天重複:", color = Color.Black)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Checkbox(
                                                checked = dailyRepeat,
                                                onCheckedChange = { dailyRepeat = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = colorResource1(id = R.color.light_blue),
                                                    uncheckedColor = Color.Gray
                                                )
                                            )
                                        }

                                }
                            )
                        }
                        if(autoSchedule)
                        {
                            IconButton(onClick = {
                                showAutoScheduleDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit, // 使用默认的编辑图标
                                    contentDescription = "Edit",
                                    tint = Color.Black // 设置图标颜色
                                )
                            }
                        }
                    }
                }



                Spacer(modifier = Modifier.height(8.dp))

                // 提醒時間選擇
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("設定提醒時間:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showAlarmTimeDialog = true },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text(alarmTime, color = Color.White)
                    }
                    if (showAlarmTimeDialog) {
                        AlarmTimeDialog(
                            alarmTime = alarmTime,
                            onAlarmTimeChanged = { newAlarmTime -> alarmTime = newAlarmTime },
                            onDismiss = { showAlarmTimeDialog = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // 是否能夠同時進行兩件事
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("是否能夠同時進行兩件事:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = disturb,
                        onCheckedChange = { disturb = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource1(id = R.color.light_blue),
                            uncheckedColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                // 重複選單
                if (!autoSchedule) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("設定重複:", color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showRepeatDialog = true },
                            colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                        ) {
                            Text(
                                text = if (repeatType.isNotBlank()) {
                                    if (repeatEndDate.isNotBlank()) {
                                        "$repeatType 直到 $repeatEndDate"
                                    } else {
                                        repeatType
                                    }
                                } else {
                                    "無"
                                },
                                color = Color.White
                            )
                        }
                    }
                    if (showRepeatDialog) {
                        RepeatDialog(
                            onRepeatSettingChanged = { newRepeatType, newRepeatEndDate ->
                                repeatType = newRepeatType
                                repeatEndDate = newRepeatEndDate.toString()
                            },
                            onDismiss = { showRepeatDialog = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                //備註
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("備註", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

            }
            //暫時解決不了把確認放在裡面沒有bug
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                ) {
                    Text("取消", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage.value = "名稱不可為空"
                            showDialog.value = true
                            return@Button
                        }
                        if (tags.isBlank()) {
                            errorMessage.value = "標籤不可為空"
                            showDialog.value = true
                            return@Button
                        }
                        if (startTime.isBlank()) {
                            errorMessage.value = "選擇開始日期不可為空"
                            showDialog.value = true
                            return@Button
                        }
                        if (endTime.isBlank()) {
                            errorMessage.value = "選擇結束日期不可為空"
                            showDialog.value = true
                            return@Button
                        }

                        if (repeatEndDate.isNotBlank() && repeatEndDate <= startTime) {
                            errorMessage.value = "重複結束日期不可早於或等於開始日期"
                            showDialog.value = true
                            return@Button
                        }




                        val startLocalTime = LocalDateTime.parse(
                            startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        )
                        val endLocalTime = LocalDateTime.parse(
                            endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        )

                        if (repeatType == "每日" && repeatEndDate.isBlank()) {
                            val repeatEndDateLocalDate = startLocalTime.plusDays(30).toLocalDate()
                            repeatEndDate = repeatEndDateLocalDate.toString()
                        }

                        if (repeatType == "每週" && repeatEndDate.isBlank()) {
                            val repeatEndDateLocalDate = startLocalTime.plusWeeks(4).toLocalDate()
                            repeatEndDate = repeatEndDateLocalDate.toString()
                        }

                        if (repeatType == "每月" && repeatEndDate.isBlank()) {
                            val repeatEndDateLocalDate = startLocalTime.plusMonths(6).toLocalDate()
                            repeatEndDate = repeatEndDateLocalDate.toString()
                        }

                        if (repeatType == "每年" && repeatEndDate.isBlank()) {
                            val repeatEndDateLocalDate = startLocalTime.plusYears(5).toLocalDate()
                            repeatEndDate = repeatEndDateLocalDate.toString()
                        }


                        if(!autoSchedule){
                            if (endLocalTime.isAfter(startLocalTime)) {
                                //以下都為非自動排程的更新
                                //在日行事曆新增事件的情況下
                                if (event == null && currentMonth == null) {
                                    evm.addEvent(
                                        name, startTime, endTime, tags, alarmTime, repeatEndDate, repeatType, duration, idealTime, shortestTime, longestTime, dailyRepeat, disturb, description
                                    )
                                }
                                //在月行事曆新增事件的情況下
                                else if (event == null && currentMonth != null) {
                                    evm.addEvent(
                                        name, startTime, endTime, tags, alarmTime, repeatEndDate, repeatType, duration, idealTime, shortestTime, longestTime, dailyRepeat, disturb, description, currentMonth
                                    )
                                }
                                //在日行事曆 只修改一個事件的情況下
                                else if (editAll == false && event != null && currentMonth == null) {
                                    evm.updateEvent(
                                        event.uid, name, startTime, endTime, tags, alarmTime, repeatEndDate, repeatType, duration, idealTime, shortestTime, longestTime, dailyRepeat, disturb, description, null, false
                                    )
                                }
                                //在月行事曆 只修改一個事件的情況下
                                else if (editAll == false && event != null && currentMonth != null) {
                                    evm.updateEvent(
                                        event.uid, name, startTime, endTime, tags, alarmTime, repeatEndDate, repeatType, duration, idealTime, shortestTime, longestTime, dailyRepeat, disturb, description, currentMonth, false
                                    )
                                }
                                //在日行事曆 修改全部同一個重複事件的情況下
                                else if (editAll == true && event != null && currentMonth == null) {
                                    evm.updateEvent(
                                        event.uid, name, startTime, endTime, tags, alarmTime, repeatEndDate, repeatType, duration, idealTime, shortestTime, longestTime, dailyRepeat, disturb, description, null, true
                                    )
                                }
                                //在月行事曆 修改全部同一個重複事件的情況下
                                else if (editAll == true && event != null && currentMonth != null) {
                                    evm.updateEvent(
                                        event.uid, name, startTime, endTime, tags, alarmTime, repeatEndDate, repeatType, duration, idealTime, shortestTime, longestTime, dailyRepeat, disturb, description, currentMonth, true
                                    )
                                }
                                onDismiss()
                            } else {
                                errorMessage.value = "結束時間必須晚於開始時間"
                                showDialog.value = true
                            }
                        }
                        else {
                            if (endLocalTime.isAfter(startLocalTime)) {
                                //如果非每日重複
                                if (!dailyRepeat) {
                                    var durationInMinutes = 0
                                    var totalFreeTimeInMinutes = 0
                                    // 呼叫 getFreeTime 方法
                                    evm.getFreeTime(startTime, endTime) { freeTimeList ->
                                        val localDateTimeSlots = freeTimeList.map { (start, end) ->
                                            val startLocalDateTime = LocalDateTime.parse(
                                                start,
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                            )
                                            val endLocalDateTime = LocalDateTime.parse(
                                                end,
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                            )
                                            startLocalDateTime to endLocalDateTime
                                        }

                                        // 印出 localDateTimeSlots 日誌
                                        localDateTimeSlots.forEach { (start, end) ->
                                            println("Free Time Slot: $start to $end")
                                        }

                                        // 計算總空閒時間
                                        val totalFreeTime =
                                            evm.calculateTotalFreeTime(localDateTimeSlots)

                                        // 印出總空閒時間日誌
                                        println("Total Free Time: $totalFreeTime")

                                        // 比較 totalFreeTime 和 duration
                                        durationInMinutes = duration.split(":").let {
                                            it[0].toInt() * 60 + it[1].toInt()
                                        }
                                        totalFreeTimeInMinutes = totalFreeTime.split(":").let {
                                            it[0].toInt() * 60 + it[1].toInt()
                                        }
                                        if (durationInMinutes > totalFreeTimeInMinutes) {
                                            errorMessage.value = "空檔時間不夠所需時間安排"
                                            showDialog.value = true
                                        } else {
                                            //如果有填理想時間
                                            if(idealTime.isNotBlank()){
                                                evm.filterSlotsByIdealTime(localDateTimeSlots,idealTime){ byIdealList ->
                                                    byIdealList.forEach{(start,end) ->
                                                        println("Ideal Time slot:$start to $end")
                                                    }
                                                }
                                            }
                                            else{
                                                //抓標籤
                                                onDismiss()
                                            }
                                        }
                                    }
                                }
                                //如果為每日重複
                                else {
                                    evm.getFreeTime(startTime, endTime) { freeTimeList ->
                                        val localDateTimeSlots = freeTimeList.map { (start, end) ->
                                            val startLocalDateTime = LocalDateTime.parse(
                                                start,
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                            )
                                            val endLocalDateTime = LocalDateTime.parse(
                                                end,
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                            )
                                            startLocalDateTime to endLocalDateTime
                                        }
                                        println("check:$freeTimeList")

                                        val endTimeDateTime = LocalDateTime.parse(
                                            endTime,
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                        )

                                        val endTimeLocal = endTimeDateTime.toLocalTime()

                                        // 使用 calculateDailyFreeTime 方法
                                        val dailyFreeTime = evm.calculateDailyFreeTime(
                                            localDateTimeSlots,
                                            endTimeLocal
                                        )

                                        // 印出每日空閒時間日誌
                                        dailyFreeTime.forEach { (date, time) ->
                                            println("Date: $date, Free Time: $time")
                                        }

                                        // 比較每日的空閒時間是否足夠
                                        val requiredMinutes = duration.split(":").let {
                                            it[0].toInt() * 60 + it[1].toInt()
                                        }

                                        // 使用 `any` 來檢查是否有任何一天的空閒時間足夠
                                        val isEnough = dailyFreeTime.values.all { freeTime ->
                                            val freeTimeInMinutes = freeTime.split(":").let {
                                                it[0].toInt() * 60 + it[1].toInt()
                                            }
                                            freeTimeInMinutes >= requiredMinutes
                                        }

                                        // 確保 `isEnough` 的計算完成後才繼續執行後面的代碼
                                        println("check:$isEnough")

                                        if (!isEnough) {
                                            errorMessage.value = "某幾天的空檔時間不夠所需時間安排"
                                            showDialog.value = true
                                        }
                                    }
                                }
                            } else {
                                errorMessage.value = "結束時間必須晚於開始時間"
                                showDialog.value = true
                            }
                        }
                    },
                    colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                ) {
                    Text("確認", color = Color.White)
                }
                // 錯誤提示對話框
                if (showDialog.value) {
                    Log.d("error", "$showDialog")
                    ErrorAlertDialog(
                        showDialog = mutableStateOf(showDialog.value),
                        message = errorMessage.value,
                        onDismiss = { showDialog.value = false }
                    )
                }
            }
        }
    }
}

//設置日期、時間對話框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val initialDate = initialDateTime.toLocalDate()
    val initialTime = initialDateTime.toLocalTime()

    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(),
        text = {
            Column(
                modifier = Modifier
                    .background(color = Color.LightGray.copy(alpha = .3f))
                    .padding(
                        top = 28.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 12.dp
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = {
                    DatePickerDialog(context, { _, year, month, dayOfMonth ->
                        date = LocalDate.of(year, month + 1, dayOfMonth)
                    }, initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth).show()
                }) {
                    Text(text = "選擇日期: ${date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    TimePickerDialog(context, { _, hour, minute ->
                        time = LocalTime.of(hour, minute)
                    }, initialTime.hour, initialTime.minute, true).show()
                }) {
                    Text(text = "選擇時間: ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDateTimeSelected(LocalDateTime.of(date, time))
            }) {
                Text(text = "確認", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "取消", color = Color.Black)
            }
        }
    )
}

//抓錯誤視窗
@Composable
fun ErrorAlertDialog(
    showDialog: MutableState<Boolean>,
    message: String,
    onDismiss: () -> Unit
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {onDismiss()},
            title = {
                Text(text = "提示")
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("確定")
                }
            }
        )
    }
}

fun localDateToLong(date: LocalDate): Long {
    return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

//標籤下拉式選單
@Composable
fun TagsDropdownMenu(
    currentTag: String,
    onTagSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = currentTag,
            onValueChange = {},
            label = { Text("標籤", color = Color.Black) },
            readOnly = true,  // 設為只讀，以避免用戶直接編輯
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,  // 這裡需要一個下拉箭頭的圖示
                        contentDescription = "選擇標籤"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            val tags = listOf("工作", "娛樂", "運動", "生活雜務", "讀書", "旅遊", "吃飯")

            tags.forEach { tag ->
                DropdownMenuItem(
                    onClick = {
                        onTagSelected(tag)
                        expanded = false
                    },
                    text = {
                        Text(tag)
                    }
                )
            }
        }
    }
}

//提醒時間選單
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTimeDialog(
    alarmTime: String,
    onAlarmTimeChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf("1天前") }  // 預設選項為“1天前”
    var showTimePicker by remember { mutableStateOf(false) }  // 控制 TimePicker 顯示狀態
    val timeState = remember { TimePickerState(initialHour = 0, initialMinute = 0, is24Hour = true) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)  // 設置背景顏色為白色
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 選擇提醒時間選項
            Column {
                RadioButton(
                    selected = selectedOption == "1小時前",
                    onClick = { selectedOption = "1小時前" }
                )
                Text(
                    text = "1小時前",
                    modifier = Modifier
                        .clickable { selectedOption = "1小時前" }
                        .padding(start = 16.dp)
                )

                RadioButton(
                    selected = selectedOption == "1天前",
                    onClick = { selectedOption = "1天前" }
                )
                Text(
                    text = "1天前",
                    modifier = Modifier
                        .clickable { selectedOption = "1天前" }
                        .padding(start = 16.dp)
                )

                RadioButton(
                    selected = selectedOption == "自訂時間",
                    onClick = {
                        selectedOption = "自訂時間"
                        showTimePicker = true
                    }
                )
                Text(
                    text = "自訂時間",
                    modifier = Modifier
                        .clickable {
                            selectedOption = "自訂時間"
                            showTimePicker = true
                        }
                        .padding(start = 16.dp)
                )
            }

            // 顯示 TimePicker
            if (showTimePicker) {
                TimePicker(state = timeState)
            }

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDismiss() }) {
                    Text(text = "取消", color = Color.Black)
                }
                TextButton(onClick = {
                    val timeString = if (selectedOption == "自訂時間") {
                        "${timeState.hour}:${timeState.minute}"
                    } else {
                        selectedOption
                    }
                    onAlarmTimeChanged(timeString)
                    onDismiss()
                }) {
                    Text(text = "確認", color = Color.Black)
                }
            }
        }
    }
}

//重複選單
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatDialog(
    onRepeatSettingChanged: (String, Any?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 重複選項
            Column {
                RadioButton(
                    selected = selectedOption == "每日",
                    onClick = { selectedOption = "每日" }
                )
                Text(text = "每日", modifier = Modifier.clickable { selectedOption = "每日" })

                RadioButton(
                    selected = selectedOption == "每週",
                    onClick = { selectedOption = "每週" }
                )
                Text(text = "每週", modifier = Modifier.clickable { selectedOption = "每週" })

                RadioButton(
                    selected = selectedOption == "每月",
                    onClick = { selectedOption = "每月" }
                )
                Text(text = "每月", modifier = Modifier.clickable { selectedOption = "每月" })

                RadioButton(
                    selected = selectedOption == "每年",
                    onClick = { selectedOption = "每年" }
                )
                Text(text = "每年", modifier = Modifier.clickable { selectedOption = "每年" })
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedOption) {
                "每日" -> DailyRepeatSetting { selectedEndDate -> endDate = selectedEndDate }
                "每週" -> WeeklyRepeatSetting { selectedEndDate -> endDate = selectedEndDate }
                "每月" -> MonthlyRepeatSetting { selectedEndDate -> endDate = selectedEndDate }
                "每年" -> YearlyRepeatSetting { selectedEndDate -> endDate = selectedEndDate }
            }

            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onDismiss() }) {
                    Text(text = "取消", color = Color.Black)
                }
                TextButton(onClick = {
                    onRepeatSettingChanged(selectedOption, endDate)
                    onDismiss()
                }) {
                    Text(text = "確認", color = Color.Black)
                }
            }
        }
    }
}

//每日重複選單
@Composable
fun DailyRepeatSetting(onEndDateSelected: (String) -> Unit) {
    var interval by remember { mutableStateOf("1") }
    var showDatePicker by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
//        TextField(
//            value = interval,
//            onValueChange = { interval = it },
//            label = { Text("每幾天重複") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("結束日期:", color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.buttonColors(Color(0xFF03A9F4))
            ) {
                Text(if (endDate.isBlank()) "選擇日期" else endDate, color = Color.White)
            }
        }
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    endDate = formatDate(year, month, dayOfMonth)
                    onEndDateSelected(endDate)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnShowListener {
                    getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK)
                    getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.LTGRAY)
                    getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(android.graphics.Color.GRAY)
                }
            }.show()
        }
    }
}

//每週重複選單
@Composable
fun WeeklyRepeatSetting(onEndDateSelected: (String) -> Unit) {
    var daysOfWeek by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var interval by remember { mutableStateOf("1") }
    var showDatePicker by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        // 顯示星期一到星期天的選擇
//        DayOfWeek.values().forEach { day ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        daysOfWeek = if (daysOfWeek.contains(day)) {
//                            daysOfWeek - day
//                        } else {
//                            daysOfWeek + day
//                        }
//                    },
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Checkbox(
//                    checked = daysOfWeek.contains(day),
//                    onCheckedChange = null
//                )
//                Text(text = day.name)
//            }
//        }
//        TextField(
//            value = interval,
//            onValueChange = { interval = it },
//            label = { Text("每幾週重複") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("結束日期:", color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
            ) {
                Text(if (endDate.isBlank()) "選擇日期" else endDate, color = Color.White)
            }
        }
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    endDate = formatDate(year, month, dayOfMonth)
                    onEndDateSelected(endDate)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnShowListener {
                    getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK)
                    getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.LTGRAY)
                    getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(android.graphics.Color.GRAY)
                }
            }.show()
        }
    }
}

//每月重複選單
@Composable
fun MonthlyRepeatSetting(onEndDateSelected: (String) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("結束日期:", color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
            ) {
                Text(if (endDate.isBlank()) "選擇日期" else endDate, color = Color.White)
            }
        }
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    endDate = formatDate(year, month, dayOfMonth)
                    onEndDateSelected(endDate)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnShowListener {
                    getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK)
                    getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.LTGRAY)
                    getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(android.graphics.Color.GRAY)
                }
            }.show()
        }
    }
}


//每年重複選單
@Composable
fun YearlyRepeatSetting(onEndDateSelected: (String) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("結束日期:", color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
            ) {
                Text(if (endDate.isBlank()) "選擇日期" else endDate, color = Color.White)
            }
        }
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    endDate = formatDate(year, month, dayOfMonth)
                    onEndDateSelected(endDate)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnShowListener {
                    getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK)
                    getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.LTGRAY)
                    getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(android.graphics.Color.GRAY)
                }
            }.show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun formatIdealTime(idealTime: TimePickerState, selectedOption: String): String {
    val hours = idealTime.hour
    val minutes = idealTime.minute
    return if (hours == 0 && minutes == 0) {
        "" // 返回空字串表示未選擇時間
    } else {
        "%02d:%02d | %s".format(hours, minutes, selectedOption)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun formatDuration(duration: TimePickerState): String {
    val hours = duration.hour
    val minutes = duration.minute
    return "%02d:%02d".format(hours, minutes)
}

fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
    return "$year-${month + 1}-$dayOfMonth"
}

fun parseTimeToMinutes(time: String): Int {
    // 初始化分鐘數
    var minutes = 0

    // 判斷是否有小時和分鐘的標誌
    val hourMatcher = Regex("""(\d+)hr""").find(time)
    val minuteMatcher = Regex("""(\d+)min""").find(time)

    // 處理小時
    hourMatcher?.let {
        minutes += it.groupValues[1].toInt() * 60
    }

    // 處理分鐘
    minuteMatcher?.let {
        minutes += it.groupValues[1].toInt()
    }

    return minutes
}

// 解析 duration 字串為小時和分鐘
fun parseDurationString(durationString: String): Pair<Int, Int> {
    return try {
        val parts = durationString.split(":")
        val hours = if (parts.size > 1) parts[0].toInt() else 0
        val minutes = if (parts.size > 1) parts[1].toInt() else 0
        Pair(hours, minutes)
    } catch (e: Exception) {
        Pair(0, 0) // 如果解析失敗，預設為 0 小時 0 分鐘
    }
}


fun parseIdealTimeString(idealTimeString: String): Pair<Pair<Int, Int>, String> {
    return try {
        val parts = idealTimeString.split("|")
        val timePart = parts[0].trim()
        val optionPart = if (parts.size > 1) parts[1].trim() else "之前" // 預設選項為 "之前"

        // 解析時間部分
        val timeParts = timePart.split(":")
        val hours = if (timeParts.size > 1) timeParts[0].toInt() else 0
        val minutes = if (timeParts.size > 1) timeParts[1].toInt() else 0

        Pair(Pair(hours, minutes), optionPart)
    } catch (e: Exception) {
        Pair(Pair(0, 0), "之前") // 如果解析失敗，預設為 0 小時 0 分鐘 和 "之前"
    }
}