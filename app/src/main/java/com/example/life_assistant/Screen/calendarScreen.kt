package com.example.life_assistant.Screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.data.EventEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun calendarScreen(
    navController: NavController,
    evm: EventViewModel,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val events by evm.events.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalendarHeader(month = currentMonth, year = currentYear)
        Spacer(modifier = Modifier.height(16.dp))
        CalendarGrid(
            month = currentMonth,
            year = currentYear,
            events = events,
            onDateSelected = { date ->
                selectedDate = date
                showAddEventDialog = true
                selectedDate?.let {
                    evm.getEventsByDate(convertDateToString(it))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        selectedDate?.let {
            SelectedDateDialog(date = it) {
                selectedDate = null
            }
        }
        if (showAddEventDialog) {
            AddEventDialog(
                date = selectedDate ?: Date(),
                evm = evm,
                onDismiss = { showAddEventDialog = false },
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        OutlinedButton(
            onClick = {
                navController.navigate(DestinationScreen.Main.route)
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffafca3)),
            border = BorderStroke(1.dp, Color(0xff534e4e)),
            modifier = Modifier
                .requiredWidth(width = 100.dp)
                .requiredHeight(height = 50.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = "登出",
                color = Color.Black.copy(alpha = 0.25f),
                fontSize = 20.sp,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CalendarHeader(month: Int, year: Int) {
    val monthNames = arrayOf(
        "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"
    )
    Text(
        text = "${monthNames[month]} $year",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun CalendarGrid(
    month: Int,
    year: Int,
    events: List<EventEntity>,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeksInMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

    Column {
        Row {
            arrayOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        for (week in 0 until weeksInMonth) {
            Row {
                for (dayOfWeek in 1..7) {
                    val dayOfMonth = (week * 7 + dayOfWeek) - firstDayOfWeek + 1
                    if (dayOfMonth > 0 && dayOfMonth <= daysInMonth) {
                        val date = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        }.time

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clickable {
                                    onDateSelected(date)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(text = dayOfMonth.toString())
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedDateDialog(date: Date, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "選擇的日期", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = SimpleDateFormat("yyyy年 MM月 dd日", Locale.CHINESE).format(date))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "確定",
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AddEventDialog(date: Date, evm: EventViewModel, onDismiss: () -> Unit) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }


    val events by evm.eventsByDate.collectAsState(emptyList())
    //Log.d("test", events.toString())

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "新增事件", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                //顯示事件
                events.forEach { event ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = event.name)
                            Text(text = event.description)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { evm.deleteEvent(event) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除事件")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("事件名稱") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("事件描述") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    evm.addEvent(eventName, eventDescription, date.time)
                    onDismiss()  // 關閉對話框
                }) {
                    Text("新增")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    }
}

fun convertDateToString(date: Date): String {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return dateFormat.format(date)
}

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    calendarScreen()
//}