package com.example.life_assistant.Screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.data.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthCalendarScreen(
    navController: NavController,
    evm: EventViewModel,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedHour by remember { mutableStateOf("") }
    var showEventDetailDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showInputDialog by remember {mutableStateOf(false)}
    var searchQuery by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf((false)) }

    val events by evm.events.observeAsState(emptyList())
    val eventsByDate = events.groupBy { LocalDate.parse(it.startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) }

    LaunchedEffect(currentMonth) {
        evm.getEventsForMonth(currentMonth)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        androidx.compose.material3.IconButton(onClick = { expanded = true }) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = R.drawable.change),
                                contentDescription = "More Options",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    navController.navigate(DestinationScreen.DailyCalendar.route)
                                },
                                text = {
                                    androidx.compose.material3.Text("日行事曆")
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    navController.navigate(DestinationScreen.Main.route)
                                },
                                text = {
                                    androidx.compose.material3.Text("個人資料")
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    navController.navigate(DestinationScreen.Classification.route)
                                },
                                text = {
                                    androidx.compose.material3.Text("標籤分類")
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    navController.navigate(DestinationScreen.TimeReport.route)
                                },
                                text = {
                                    androidx.compose.material3.Text("行程分析")
                                }
                            )
//                            androidx.compose.material3.DropdownMenuItem(
//                                onClick = {
//                                    expanded = false
//                                    navController.navigate(DestinationScreen.WeekCalendar.route)
//                                },
//                                text = {
//                                    androidx.compose.material3.Text("週行事曆")
//                                }
//                            )
                            androidx.compose.material3.DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    mvm.logout()
                                },
                                text = {
                                    androidx.compose.material3.Text("登出")
                                }
                            )
                        }
                    }
                    IconButton(onClick = {
                        currentMonth = currentMonth.minusMonths(1)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month"
                        )
                    }
                    Text(
                        text = "${currentMonth.month.name} ${currentMonth.year}",
                        style = MaterialTheme.typography.h6
                    )
                    IconButton(onClick = {
                        currentMonth = currentMonth.plusMonths(1)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month"
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = {
                        showDialog = true
                        evm.habitEvents { habit ->
                            println("$habit")
                        }
                    }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Event",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        MonthBody(
            evm = evm,
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                Log.d("date","$selectedDate")
                navController.navigate("daily_calendar_screen/${date.format(formatter)}")
            },
            onEventSelected = { event ->
                selectedEvent = event
                showEventDetailDialog = true
            },
            eventsByDate = eventsByDate
        )
    }

    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    androidx.compose.material3.Text(text = "自動排程")
                    androidx.compose.material3.IconButton(onClick = {
                        showDialog = false
                        // Trigger the UserInputDialog here
                        showInputDialog = true
                    }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Event"
                        )
                    }
                }
            },
            text = {
                Column {
                    androidx.compose.material3.TextField(
                        value = searchQuery,
                        onValueChange = { query -> searchQuery = query },
                        label = { androidx.compose.material3.Text("輸入事件") }
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        evm.checkEvent(searchQuery) { count ->
                            Log.d("EventCount", "找到 $count 筆相似事件")
                            if (count >= 10) {
                                evm.classifyEventsFromFirebase(searchQuery) { classification ->
                                    println("分類後:$classification")
                                }
                            } else {
                                alertMessage = "所需的相似資料數量不足，請優先使用原本的自動排程"
                                showErrorDialog = true
                            }
                        }
                        showDialog = false
                    }
                ) {
                    androidx.compose.material3.Text("確認")
                }
            },
            dismissButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    androidx.compose.material3.Text("取消")
                }
            }
        )
    }


    if (showInputDialog) {
        UserInputDialog(
            selectedDate = selectedDate,  // 使用選擇的日期來顯示對話框
            evm = evm,
            onDismiss = { showInputDialog = false },
            selectedHour = selectedHour
        )
    }

    if (showErrorDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showErrorDialog = false }, // 使用者點擊外部時關閉
            title = { androidx.compose.material3.Text(text = "提示") },
            text = { androidx.compose.material3.Text(text = alertMessage) },
            confirmButton = {
                androidx.compose.material3.Button(onClick = { showErrorDialog = false }) {
                    androidx.compose.material3.Text(text = "確定")
                }
            }
        )
    }

    selectedEvent?.let { event ->
        if (showEventDetailDialog) {
            EventDetailDialog(
                event = event,
                evm = evm,
                temp = "month",
                onDismiss = { showEventDetailDialog = false },
                currentMonth = currentMonth
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthBody(
    evm: EventViewModel,
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (Event) -> Unit,
    eventsByDate: Map<LocalDate, List<Event>>
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysInMonth = currentMonth.month.length(currentMonth.isLeapYear)
    val firstDayOfMonth = currentMonth.withDayOfMonth(1).dayOfWeek.value % 7

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(firstDayOfMonth) {
                Box(modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
                )
            }

            items(daysInMonth) { day ->
                val date = currentMonth.withDayOfMonth(day + 1)
                val dayEvents = eventsByDate.values.flatten().filter { event ->
                    val eventStartDate = LocalDateTime.parse(event.startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    val eventEndDate = LocalDateTime.parse(event.endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                    // 检查事件是否与当月日期相交
                    isDateInMonth(date, eventStartDate.toLocalDate(), eventEndDate.toLocalDate())
                }

                DayCell(
                    day = date,
                    isSelected = date == selectedDate,
                    events = dayEvents,
                    onDateSelected = onDateSelected,
                    onEventSelected = onEventSelected,
                    getColorByEvent = { tag, callback ->
                        evm.getColorByEvent(tag, callback)
                    }
                )
            }
        }
    }
}

fun isDateInMonth(date: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
    return !date.isBefore(startDate) && !date.isAfter(endDate)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCell(
    day: LocalDate,
    isSelected: Boolean,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (Event) -> Unit,
    getColorByEvent: (String, (Long?) -> Unit) -> Unit // 新增取得顏色的函數參數
) {
    val isToday = day == LocalDate.now()
    val isHoliday = day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY

    Column(
        modifier = Modifier
            .padding(4.dp)
            .size(100.dp)
            .clickable { onDateSelected(day) }
            .border(
                width = 2.dp,
                color = if (isSelected) colorResource(id = R.color.light_blue) else Color.Transparent,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                color = when {
                    isHoliday -> Color.Red
                    else -> Color.Black
                },
                style = MaterialTheme.typography.body1
            )
        }

        // 顯示事件
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 4.dp) // Add padding to the bottom of the list
            ) {
                items(events) { event ->
                    var eventColor by remember { mutableStateOf(Color.LightGray) } // 預設顏色
                    getColorByEvent(event.tags) { colorValue ->
                        colorValue?.let {
                            eventColor = Color(it)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(eventColor, RoundedCornerShape(4.dp)) // 使用取得的顏色
                            .clickable { onEventSelected(event) }
                    ) {
                        Text(
                            text = event.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(4.dp)
                        )
                    }
                }
                if (events.size > 2) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    // Handle "more..." click event
                                    // This could open a detailed view of the day's events
                                }
                        ) {
                            Text(
                                text = "more...",
                                color = colorResource(id = R.color.light_blue),
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.Center) // Align the text to the center of the box
                            )
                        }
                    }
                }
            }
        }
    }
}

