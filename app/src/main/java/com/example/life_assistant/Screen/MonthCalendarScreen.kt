package com.example.life_assistant.Screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.data.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

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
    var expanded by remember { mutableStateOf(false) } // 控制下拉選單的狀態
    var selectedDate by remember { mutableStateOf(LocalDate.now()) } // 用來存儲所選日期
    var selectedHour by remember { mutableStateOf("") }

    Column {
        // Header for month
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
                    IconButton(onClick = {
                        selectedDate = LocalDate.now() // 設置當前日期為選擇日期
                        selectedHour = LocalTime.now().hour.toString().padStart(2, '0') // 設置當前小時為選擇小時
                        showDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Event",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                MonthBody(evm, currentMonth, selectedDate) { date ->
                    selectedDate = date
                    selectedHour = LocalTime.now().hour.toString().padStart(2, '0')
                    showDialog = true
                }
            }
        }
    }
    // 顯示對話框
    if (showDialog) {
        UserInputDialog(
            selectedDate = selectedDate,
            evm = evm,
            onDismiss = { showDialog = false },
            selectedHour = selectedHour
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthBody(
    evm: EventViewModel,
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysInMonth = currentMonth.month.length(currentMonth.isLeapYear)
    val firstDayOfMonth = currentMonth.withDayOfMonth(1).dayOfWeek.value % 7
    val events by evm.events.observeAsState(emptyList())

    Column {
        // Header for days of the week
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
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

        // Days of the month
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(8.dp)
        ) {
            // Fill in the leading empty cells
            items(firstDayOfMonth) {
                Box(modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                )
            }

            // Fill in the days of the month
            items(daysInMonth) { day ->
                DayCell(
                    day = currentMonth.withDayOfMonth(day + 1),
                    isSelected = currentMonth.withDayOfMonth(day + 1) == selectedDate,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCell(
    day: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val isToday = day == LocalDate.now()
    val isCurrentMonth = day.month == LocalDate.now().month
    val isHoliday = isCurrentMonth && (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY)

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .clickable { onDateSelected(day) }
            .background(
                color = if (isSelected) colorResource(id = R.color.light_blue) else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = if (isToday) colorResource(id = R.color.light_blue) else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.dayOfMonth.toString(),
                color = when {
                    isHoliday -> Color.Red // 假日字體顯示紅色
                    isToday -> Color.Black
                    isCurrentMonth -> Color.Black
                    else -> Color.Gray
                }
            )
        }
    }
}