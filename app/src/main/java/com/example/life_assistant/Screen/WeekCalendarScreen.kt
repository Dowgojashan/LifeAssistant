package com.example.life_assistant.Screen


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyCalendarView(
    navController: NavController,
    evm: EventViewModel,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier,
    currentDay: LocalDate,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onSwitchToMonthView: () -> Unit,
) {
    // 紀錄選擇的日期
    val selectedDate = remember { mutableStateOf(currentDay) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf("") }

    // 管理使用者輸入的行事曆項目
    val currentDate = remember { mutableStateOf(LocalDate.now()) }


    Scaffold(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 月視圖和新增事件按鈕
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 切換至月視圖的按鈕
                IconButton(
                    onClick = onSwitchToMonthView,
                    modifier = modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.change),
                        contentDescription = "Change to Month View",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 顯示當前月份和年份的文字
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        "${currentDay.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentDay.year}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // 新增事件的按鈕
                IconButton(
                    onClick = { showDialog = true } ,
                    modifier = modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "Add Event",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 星期顯示，靠右對齊
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(5f)) // 推動星期顯示靠右
                // 顯示選擇日期的前後五天的星期縮寫
                for (i in -2..2) {
                    val dayOfWeek = selectedDate.value.plusDays(i.toLong()).dayOfWeek
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // 日期行，顯示選擇日期的前後五天
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(5f)) // 推動星期顯示靠右
                for (i in -2..2) {
                    val date = selectedDate.value.plusDays(i.toLong())
                    DayCell(
                        day = date,
                        isToday = date == LocalDate.now(),
                        isSelected = selectedDate.value == date,
                        onDateSelected = { selectedDate.value = it }
                    )
                }
            }

            // 每小時時間軸，顯示選擇日期的前後兩天共五天的每小時事件
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(24) { hour ->
                    val hourString = hour.toString().padStart(2, '0')
                    HourlyRow(hourString, selectedDate.value)
                }
            }

//            if (showDialog) {
//                UserInputDialog(
//                    onDismiss = { showDialog = false },
//                    onConfirm = { name, startTime, endTime, date, tags, notes, alarmTime, repeat ->
//                        userEntries.add(    //確定新增事件，將事件添加到事件列表
//                            UserEntry(
//                                name,
//                                startTime,
//                                endTime,
//                                date,
//                                tags,
//                                notes,
//                                alarmTime.toString(),
//                                repeat
//                            )
//                        )
//                        showDialog = false
//                    },
//                    selectedHour = selectedHour
//                )
//            }

            // 當天行程
            //Text("下面是當天行程", style = MaterialTheme.typography.bodyLarge)
            //DailyEventsView(selectedDate.value)
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
        isToday -> Color.Blue
        isSelected -> Color.LightGray
        else -> Color.Transparent
    }
    val textColor = if (isToday) Color.Red else Color.Black

    Box(
        modifier = Modifier
            .padding(horizontal = 15.dp, vertical = 6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(colorResource(id = R.color.light_blue))
            .clickable { onDateSelected(day) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            color = textColor
        )
    }
}

/*@RequiresApi(Build.VERSION_CODES.O)  //目前沒用到
@Composable
fun DailyEventsView(currentDay: LocalDate) {
    val events = remember {
        mutableStateListOf(
            Event("睡覺", LocalDate.now(), 1),
            Event("睡覺", LocalDate.now().plusDays(1), 1),
            Event("睡覺", LocalDate.now().plusDays(2), 1),
            //Event("睡覺", LocalDate.now().plusDays(3), 1),
            //Event("睡覺", LocalDate.now().plusDays(4), 1),

        )
    }// 示例事件

}*/



@RequiresApi(Build.VERSION_CODES.O)
@Composable  //時間軸
fun HourlyRow(hour: String, selectedDate: LocalDate) {
    // 選擇的日期為中心，加減兩天共五天
    val days = (-2..2).map { selectedDate.plusDays(it.toLong()) }
    val hourInt = hour.toInt()

    Column {
        Divider (  //每列分隔線
            color = Color.Black,
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
            //.bottomBorder(1.dp, Color.Black)
        ) {
            // 顯示每小時的時間
            Text(
                text = "$hour:00",
                modifier = Modifier
                    .width(60.dp)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Divider(modifier = Modifier.width(1.dp)) //時數跟事件表的分隔線
            // 顯示每小時的事件框
//            for (day in days) {
//                val dayEvents = events.filter { it.date == day && it.startTime.hour <= hourInt && it.endTime.hour >= hourInt }
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(40.dp)
//                        .background(Color.White)
//                ) {
//                    // 查找事件列表中符合當前日期和小時的事件
//                    //val event = events.find { it.date == day && (it.startTime.hour <= hourInt && it.endTime.hour >= hourInt) }
//                    //event?.let {
//                    Column(
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        // 使用 items 函數顯示事件
//                        LazyColumn(
//                            verticalArrangement = Arrangement.Center,
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            items(dayEvents.size) {  index ->
//                                val event = dayEvents[index]
//                                Column(
//                                    //modifier = Modifier.align(Alignment.Center),
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Text(
//                                        text = event.name,
//                                        color = Color.Black,
//                                        modifier = Modifier
//                                            .background(Color.LightGray)
//                                            .padding(4.dp)
//                                    )
//                                    Text(
//                                        text = "${event.startTime} - ${event.endTime}",
//                                        color = Color.Gray,
//                                        fontSize = 12.sp
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
                Divider(modifier = Modifier.width(1.dp))  //天數分隔線

        }
    }
}