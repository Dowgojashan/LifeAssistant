package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.life_assistant.R
import java.util.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import androidx.compose.ui.res.colorResource as colorResource1

// 使用者事件的資料類別
data class UserEntry(
    val name: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val alarmTime: String,
    val repeat: String = ""
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getStartTime(): LocalTime {
        return LocalTime.parse(startTime, DateTimeFormatter.ofPattern("H:mm"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEndTime(): LocalTime {
        return LocalTime.parse(endTime, DateTimeFormatter.ofPattern("H:mm"))
    }
}

// 儲存顯示在日程中的事件
data class Event(
    val name: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DaliyCalender(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf("") }
    val userEntries = remember { mutableStateListOf<UserEntry>() }
    val currentDate = remember { mutableStateOf(LocalDate.now()) }

    // 將 UserEntry 轉換為 Event 類型，這樣可以統一顯示
    val events = userEntries.map { entry ->
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        val date = LocalDate.parse(entry.date, formatter)
        Event(
            name = entry.name,
            date = date,
            startTime = entry.getStartTime(),
            endTime = entry.getEndTime(),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WeeklyCalendarView(
            currentDay = currentDate.value,
            events = events,
            onNextWeek = { currentDate.value = currentDate.value.plusWeeks(1) },
            onPreviousWeek = { currentDate.value = currentDate.value.minusWeeks(1) },
            onSwitchToMonthView = { /* 切換到月視圖 */ },
            onAddEvent = { showDialog = true } // 當長按時，顯示添加事件的對話框
        )

        Spacer(modifier = Modifier.height(16.dp))
        //長按畫面可以新增行程
        if (showDialog) {
            UserInputDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, startTime, endTime, date, tags, notes, alarmTime,
                              repeat ->
                    userEntries.add(
                        UserEntry(
                            name,
                            startTime,
                            endTime,
                            date,
                            tags,
                            notes,
                            alarmTime.toString(),
                            repeat
                        )
                    )
                    showDialog = false
                },
                selectedHour = selectedHour
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyRow(hour: String, selectedDate: LocalDate, events: List<Event>) {
    val day = selectedDate
    val hourInt = hour.toInt()
//時間軸的部分
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
        Divider(modifier = Modifier.width(1.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(Color.White)
        ) {
            // 檢查事件是否在當前小時內
            val event = events.find { it.date == day && (it.startTime.hour <= hourInt && it.endTime.hour >= hourInt) }
            event?.let {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it.name,
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color.LightGray)
                            .padding(4.dp)
                    )
                    Text(
                        text = "${it.startTime} - ${it.endTime}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Divider(modifier = Modifier.width(1.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell( //上面那一排日期
    day: LocalDate,
    isToday: Boolean,
    isSelected: Boolean = false,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor = when {
        isToday -> colorResource1(id = R.color.light_blue)
        isSelected -> Color.LightGray
        else -> Color.Transparent
    }
    val textColor = if (isToday) Color.Red else Color.Black

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onDateSelected(day) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            color = textColor
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeeklyCalendarView(
    modifier: Modifier = Modifier,
    currentDay: LocalDate,
    events: List<Event>,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onSwitchToMonthView: () -> Unit,
    onAddEvent: () -> Unit
) {
    val selectedDate = remember { mutableStateOf(currentDay) }
    var showDialog by remember { mutableStateOf(false) }
    var longPressedHour by remember { mutableStateOf("") }

    Scaffold(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row( //轉換跟加和2024那一橫排
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Icon(
                        painter = painterResource(id = R.drawable.change),
                        contentDescription = "Previous Week",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    "${currentDay.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentDay.year}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                IconButton(onClick = onNextWeek) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "Next Week",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Row( //mon-sun
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
                val startOfWeek = currentDay.with(DayOfWeek.MONDAY)
                (0..6).forEach { i ->
                    val date = startOfWeek.plusDays(i.toLong())
                    DayCell(
                        day = date,
                        isToday = date == LocalDate.now(),
                        isSelected = selectedDate.value == date,
                        onDateSelected = { selectedDate.value = it }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showDialog = true
                                onAddEvent()
                            },
                            onLongPress = { offset ->
                                val hour = (offset.y / 60).toInt()
                                longPressedHour = hour.toString().padStart(2, '0')
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
                                            showDialog = true
                                            onAddEvent()
                                        },
                                        onLongPress = { offset ->
                                            val hour = (offset.y / 60).toInt()
                                            longPressedHour = hour.toString().padStart(2, '0')
                                            showDialog = true
                                        }
                                    )
                                }
                        )
                        {
                            DailyRow(hourString, selectedDate.value, events)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        UserInputDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, startTime, endTime, date, tags, notes, alarmTime, repeat ->
                showDialog = false
            },
            selectedHour = longPressedHour
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, List<String>, String, Boolean, String) -> Unit,
    selectedHour: String
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var startTime by remember { mutableStateOf(selectedHour) }
    var endTime by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(TextFieldValue("")) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    var alarmTime by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var autoSchedule by remember { mutableStateOf(false) }
    var showStartTimeDialog by remember { mutableStateOf(false) }
    var showEndTimeDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAlarmTimeDialog by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        initialHour = 0,
        initialMinute = 0
    )

    // Function to format the date to a string
    fun formatDate(year: Int, month: Int, day: Int): String {
        return "$day/${month + 1}/$year"
    }
    //新增事件，基本上那個框框的順序跟這個裡面是一致得
    AlertDialog(
        modifier = Modifier.wrapContentWidth(),
        onDismissRequest = onDismiss,
        title = { Text(text = "新增事件", color = Color.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("事件名稱", color = Color.Black) }
                )
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
                        AlertDialog(
                            onDismissRequest = { showStartTimeDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(color = Color.LightGray.copy(alpha = .3f))
                                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
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
                                        startTime = "${timeState.hour}:${timeState.minute}"
                                    }) {
                                        Text(text = "確認", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
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
                        AlertDialog(
                            onDismissRequest = { showEndTimeDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(color = Color.LightGray.copy(alpha = .3f))
                                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
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
                                        endTime = "${timeState.hour}:${timeState.minute}"
                                    }) {
                                        Text(text = "確認", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("標籤 (逗號分隔)", color = Color.Black) }
                )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("設定提醒時間:", color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showAlarmTimeDialog = true },
                        colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
                    ) {
                        Text(alarmTime.ifBlank { "選擇提醒時間" }, color = Color.White)
                    }
                    if (showAlarmTimeDialog) {
                        AlertDialog(
                            onDismissRequest = { showAlarmTimeDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(color = Color.LightGray.copy(alpha = .3f))
                                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TimePicker(state = timeState)
                                Row(
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .fillMaxWidth(), horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showAlarmTimeDialog = false }) {
                                        Text(text = "取消", color = Color.Black)
                                    }
                                    TextButton(onClick = {
                                        showAlarmTimeDialog = false
                                        alarmTime = "${timeState.hour}:${timeState.minute}"
                                    }) {
                                        Text(text = "確認", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = repeat,
                    onValueChange = { repeat = it },
                    label = { Text("重複", color = Color.Black) }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("備註", color = Color.Black) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name.text,
                        startTime,
                        endTime,
                        date,
                        tags.text.split(",").map { it.trim() },
                        notes.text,
                        autoSchedule,
                        alarmTime
                    )
                },
                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
            ) {
                Text("新增事件", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.run { buttonColors(colorResource1(id = R.color.light_blue)) }
            ) {
                Text("取消", color = Color.White)
            }
        }
    )
}




@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun DaliyCalenderPreview() {
    DaliyCalender()
}
