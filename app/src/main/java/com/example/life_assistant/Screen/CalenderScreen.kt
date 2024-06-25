package com.example.life_assistant.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.ViewModel.CalendarEvent
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    navController: NavHostController,
    mvm: MemberViewModel
) {
    val daysInMonth = 30 // 假设每个月30天
    val columns = 7 // 一周7天
    var selectedDate by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(false) }
    var lastClickTimestamp by remember { mutableStateOf(0L) }
    val eventsOfDay = remember { mutableStateListOf<CalendarEvent>() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Select a Date", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column {
                for (week in 0..(daysInMonth / columns)) {
                    Row {
                        for (day in 1..columns) {
                            val date = week * columns + day
                            if (date <= daysInMonth) {
                                val events = mvm.getEventsForDate(date)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            val currentTimestamp = System.currentTimeMillis()
                                            if (currentTimestamp - lastClickTimestamp < 300) { // 300 milliseconds threshold for double click
                                                selectedDate = date
                                                showDialog = true
                                                eventsOfDay.clear()
                                                eventsOfDay.addAll(events)
                                            }
                                            lastClickTimestamp = currentTimestamp
                                        }
                                        .shadow(8.dp, shape = MaterialTheme.shapes.medium)
                                ) {
                                    BasicText("$date")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        EventsDialog(
            date = selectedDate,
            onDismiss = { showDialog = false },
            events = eventsOfDay,
            mvm = mvm
        )
    }
}



@Composable
fun EventsDialog(
    date: Int,
    onDismiss: () -> Unit,
    events: MutableList<CalendarEvent>,
    mvm: MemberViewModel
) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Events for $date",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 显示当天的事件
                events.forEach { event ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            "${event.name} - ${event.description}",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            mvm.deleteEvent(event)
                            // 从 events 列表中移除事件
                            val updatedEvents = events.toMutableList()
                            updatedEvents.remove(event)
                            // 更新本地状态
                            events.clear()
                            events.addAll(updatedEvents)
                        }) {
                            Text("Delete")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Add New Event",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Event Description") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (eventName.isNotBlank() && eventDescription.isNotBlank()) {
                            mvm.addEvent(date, eventName, eventDescription)
                            events.add(CalendarEvent(date, eventName, eventDescription)) // 添加到本地事件列表
                            eventName = ""
                            eventDescription = ""
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
