package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.data.Event
import java.util.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import androidx.compose.ui.res.colorResource as colorResource1

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DailyCalendarScreen(
    navController: NavController,
    evm: EventViewModel,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }  // 設置為 LocalDate.now() 以確保能夠顯示當前日期
    var selectedHour by remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf(LocalDate.now()) }  // 這個值應該用於顯示當前月份的視圖
    var expanded by remember { mutableStateOf(false) } // 控制下拉選單的狀態
    val events by evm.events.observeAsState(emptyList())

    LaunchedEffect(selectedDate) {
        evm.getEventsForDate(selectedDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日")))
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
                                // 切換到月視圖的邏輯
                            },
                            text = {
                                Text("月行事曆")
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
                val startOfWeek = currentDate.value.with(DayOfWeek.MONDAY)
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
                                selectedHour = hour.toString().padStart(2, '0')
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
                            DailyRow(hourString,evm ,events)
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
fun DailyRow(hour: String, evm:EventViewModel, events: List<Event>) {
    val hourInt = hour.toInt()
    val eventsForHour = events.filter { event ->
        val eventStartTime = LocalTime.parse(event.startTime, DateTimeFormatter.ofPattern("HH:mm"))
        val eventEndTime = LocalTime.parse(event.endTime, DateTimeFormatter.ofPattern("HH:mm"))
        eventStartTime.hour == hourInt || eventEndTime.hour == hourInt || (eventStartTime.isBefore(LocalTime.of(hourInt, 59)) && eventEndTime.isAfter(LocalTime.of(hourInt, 0)))
    }

    Log.d("test", "$eventsForHour")

    var selectedEvent by remember { mutableStateOf<Event?>(null) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                .height(40.dp)
                .background(Color.White)
        ) {
            Column {
                eventsForHour.forEach { event ->
                    val eventStartTime = LocalTime.parse(event.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val eventEndTime = LocalTime.parse(event.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                    val timeSpan = ((eventEndTime.hour - eventStartTime.hour) * 60 + eventEndTime.minute - eventStartTime.minute).toFloat()
                    val widthFraction = (timeSpan / 60) // 每小時分為1段，長度從 0 到 1
                    val fiveWidthFraction = 1f / 5

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .fillMaxWidth(fiveWidthFraction * widthFraction) // 根據時間跨度調整寬度
                            .height(30.dp)
                            .background(Color.Blue)
                            .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .padding(4.dp)
                            .clickable { selectedEvent = event } // 添加點擊事件
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.width(1.dp))
    }

    selectedEvent?.let { event ->
        EventDetailDialog(event = event,evm,onDismiss = { selectedEvent = null })
    }
}

//事件視窗
@Composable
fun EventDetailDialog(event: Event, evm: EventViewModel, onDismiss: () -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    val updatedEvent = remember { mutableStateOf(event) }

    // 觀察 LiveData 中的 events 變化
    val events by evm.events.observeAsState(emptyList())

    // 監控事件列表變化，當事件列表更新時，更新 updatedEvent 的值
    LaunchedEffect(events) {
        updatedEvent.value = events.find { it.uid == event.uid } ?: event
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
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "編輯", tint = Color.Blue)
                    }
                    IconButton(onClick = {
                        evm.deleteEventFromFirebase(event) {
                            onDismiss() // 成功刪除後關閉視窗
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

                val replacedate = updatedEvent.value.date.replace("\n", "")
                Text("日期: ${replacedate}", color = Color.Black)
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
    if (showEditDialog) {
        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
        val replacedate = updatedEvent.value.date.replace("\n", "")
        UserInputDialog(
            selectedDate = LocalDate.parse(replacedate, formatter),
            evm = evm,
            onDismiss = { showEditDialog = false },
            selectedHour = updatedEvent.value.startTime,
            event = updatedEvent.value // 傳遞更新後的事件對象給編輯視窗
        )
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
    selectedHour: String,event: Event? = null
) {
    val context = LocalContext.current

    // 確保 selectedHour 有有效的時間值
    val initialStartTime = if (selectedHour.isNotBlank()) {
        if (selectedHour.length == 2) "$selectedHour:00" else selectedHour
    } else {
        "00:00"
    }
    val initialStartLocalTime = try {
        LocalTime.parse(initialStartTime)
    } catch (e: DateTimeParseException) {
        LocalTime.of(0, 0)  // 如果無法解析 selectedHour，則設置為默認時間 "00:00"
    }

    // 如果是編輯狀態，設置初始值
    val initialName = event?.name ?: ""
    val initialDescription = event?.description ?: ""
    val initialTags = event?.tags ?: ""
    val initialAlarmTime = event?.alarmTime ?: "1天前"
    val initialEndTimeEvent = event?.endTime ?: initialStartLocalTime.plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"))

    var name by remember { mutableStateOf(initialName) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTimeEvent) }
    var date by remember { mutableStateOf(selectedDate.format(DateTimeFormatter.ofPattern("M月d日"))) }
    var description by remember { mutableStateOf(initialDescription) }
    var alarmTime by remember { mutableStateOf(initialAlarmTime) }
    var autoSchedule by remember { mutableStateOf(false) }
    var showStartTimeDialog by remember { mutableStateOf(false) }
    var showEndTimeDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAlarmTimeDialog by remember { mutableStateOf(false) }
    var tags by remember { mutableStateOf(initialTags) }  // 當前標籤
    var showTagMenu by remember { mutableStateOf(false) }  // 控制標籤選單的顯示
    var showRepeatDialog by remember { mutableStateOf(false) }
    var repeatSetting by remember { mutableStateOf("無") }
    var showDialog = mutableStateOf(false)
    var errorMessage = mutableStateOf("")
    val timeState = rememberTimePickerState(
        initialHour = initialStartLocalTime.hour,
        initialMinute = initialStartLocalTime.minute
    )

    // Function to format the date to a string
    fun formatDate(year: Int, month: Int, day: Int): String {
        return "${month + 1}月${day}日"
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
                //設定事件名稱
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("事件名稱", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                //設定日期
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("日期:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text(if (date.isBlank()) "選擇日期" else date, color = Color.White)
                    }
                    if (showDatePicker) {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                date = formatDate(year, month, dayOfMonth)
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

                Spacer(modifier = Modifier.height(8.dp))

                //設定開始時間
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("開始時間:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showStartTimeDialog = true },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text(startTime.ifBlank { "選擇開始時間" }, color = Color.White)
                    }
                    if (showStartTimeDialog) {
                        BasicAlertDialog(
                            onDismissRequest = { showStartTimeDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                TimePicker(state = timeState)
                                Row(
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .fillMaxWidth(), horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showStartTimeDialog = false }) {
                                        Text(text = "取消", color = Color.Black)
                                    }
                                    TextButton(onClick = {
                                        showStartTimeDialog = false
                                        val startLocalTime = LocalTime.of(timeState.hour, timeState.minute)
                                        startTime = startLocalTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    }) {
                                        Text(text = "確認", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                //設定結束時間
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("結束時間:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showEndTimeDialog = true },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text(endTime.ifBlank { "選擇結束時間" }, color = Color.White)
                    }
                    if (showEndTimeDialog) {
                        BasicAlertDialog(
                            onDismissRequest = { showEndTimeDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                TimePicker(state = timeState)
                                Row(
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .fillMaxWidth(), horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showEndTimeDialog = false }) {
                                        Text(text = "取消", color = Color.Black)
                                    }
                                    TextButton(onClick = {
                                        showEndTimeDialog = false
                                        val endLocalTime = LocalTime.of(timeState.hour, timeState.minute)
                                        endTime = endLocalTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    }) {
                                        Text(text = "確認", color = Color.Black)
                                    }
                                }
                            }
                        }
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

                //自動排程(待修正)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("自動排程:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = autoSchedule,
                        onCheckedChange = { autoSchedule = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource1(id = R.color.light_blue),
                            uncheckedThumbColor = Color.Gray
                        )
                    )
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

                // 重複選單
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("設定重複:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showRepeatDialog = true },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text(repeatSetting.ifBlank { "無" }, color = Color.White)
                    }
                }
                if (showRepeatDialog) {
                    RepeatDialog(
                        onRepeatSettingChanged = { newRepeatSetting -> repeatSetting = newRepeatSetting },
                        onDismiss = { showRepeatDialog = false }
                    )
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
                            val startLocalTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"))
                            val endLocalTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))

                            if (endLocalTime.isAfter(startLocalTime)) {
                                if (event == null) {
                                    evm.addEvent(name, localDateToLong(selectedDate), startTime, endTime, tags, alarmTime, repeatSetting, description)
                                } else {
                                    evm.updateEvent(event.uid,name, localDateToLong(selectedDate), startTime, endTime, tags, alarmTime, repeatSetting, description)
                                }
                                onDismiss()
                            } else {
                                errorMessage.value = "結束時間必須晚於開始時間"
                                showDialog.value = true
                            }
                        },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text("確認", color = Color.White)
                    }
                    // 錯誤提示對話框
                    if (showDialog.value) {
                        Log.d("error","$showDialog")
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
            val tags = listOf("運動", "讀書", "吃飯")

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
    onRepeatSettingChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf("無") }

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
                "每日" -> DailyRepeatSetting()
                "每週" -> WeeklyRepeatSetting()
                "每月" -> MonthlyRepeatSetting()
                "每年" -> YearlyRepeatSetting()
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
                    onRepeatSettingChanged(selectedOption)
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
fun DailyRepeatSetting() {
    var interval by remember { mutableStateOf("1") }
    var showDatePicker by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        TextField(
            value = interval,
            onValueChange = { interval = it },
            label = { Text("每幾天重複") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
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
fun WeeklyRepeatSetting() {
    var daysOfWeek by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var interval by remember { mutableStateOf("1") }
    var showDatePicker by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        // 顯示星期一到星期天的選擇
        DayOfWeek.values().forEach { day ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        daysOfWeek = if (daysOfWeek.contains(day)) {
                            daysOfWeek - day
                        } else {
                            daysOfWeek + day
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = daysOfWeek.contains(day),
                    onCheckedChange = null
                )
                Text(text = day.name)
            }
        }
        TextField(
            value = interval,
            onValueChange = { interval = it },
            label = { Text("每幾週重複") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
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
fun MonthlyRepeatSetting() {
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
fun YearlyRepeatSetting() {
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

fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
    return "$year-${month + 1}-$dayOfMonth"
}