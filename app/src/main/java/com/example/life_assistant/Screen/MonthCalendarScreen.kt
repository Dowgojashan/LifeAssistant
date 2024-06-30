package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import java.time.LocalDate
import java.util.Locale
import java.time.format.TextStyle
import java.time.DayOfWeek

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthCalendarScreen(
    navController: NavController,
    evm: EventViewModel,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier,
    displayMonth: LocalDate,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit
) {
    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }

    // 確認 setDisplayMonth 是否被正確調用
    LaunchedEffect(displayMonth) {
        Log.d("CalendarMonthView", "Display month changed to: $displayMonth")
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 显示月份和星期
        CalendarHeader(displayMonth, onNextMonth, onPreviousMonth)

        // 创建一个月视图行事历
        CalendarGrid(selectedDate = selectedDate, displayMonth = displayMonth) { date ->
            selectedDate.value = date
        }
    }
}

// 显示月份和切换控件的组件
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarHeader(
    displayMonth: LocalDate,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit
) {
    val monthName = "${displayMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayMonth.year}"    //月曆標題，eg. 6月2024

    Column {
        // 显示月份和切换按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {   //跳到上一個月按鈕
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Month"
                )
            }

            Text(
                text = monthName,     //月曆標題，eg. 6月2024
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            IconButton(onClick = onNextMonth ) {     //跳到下一個月按鈕
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Month"
                )
            }
        }

        // 显示星期
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in DayOfWeek.entries) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// 日历网格
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    selectedDate: MutableState<LocalDate?>,
    displayMonth: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    // 获取指定月份的日期列表
    val daysInMonth = remember(displayMonth) { CalendarUtils.getDaysInMonth(displayMonth) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
        //.padding(horizontal = 16.dp)
    ) {
        itemsIndexed(daysInMonth.chunked(7)) { index, week ->
            // 每周行
            Row(modifier = Modifier.fillMaxWidth()) {
                // 每日格子
                for (day in week) {
                    DayCell(
                        day = day,
                        isSelected = selectedDate.value?.isEqual(day) ?: false,  //判斷某一天是否被選中
                        onDateSelected = { date -> onDateSelected(date) }   //在某一天被選中時調用回調函數來處理選中事件
                    )
                }
            }
        }
    }
}

// 单个日期单元格
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCell(
    day: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val isToday = day == LocalDate.now()
    val isCurrentMonth = day.month == LocalDate.now().month
    val isHoliday = isCurrentMonth && (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) // 判斷是否為當月的假日

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .clickable { onDateSelected(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(    //當日日期有藍色圓圈背景
                        color = if (isToday)  colorResource(id = R.color.light_blue) else Color.Transparent,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.dayOfMonth.toString(),
                    color = when {
                        isHoliday -> Color.Red // 假日字體顯示紅色
                        isToday -> Color.Black
                        isSelected -> Color.Blue
                        isCurrentMonth -> Color.Black
                        else -> Color.Gray
                    }
                )
            }
        }
    }
}

// 工具类用于获取指定月份的日期
object CalendarUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDaysInMonth(month: LocalDate): List<LocalDate> {
        val firstOfMonth = month.withDayOfMonth(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 获取当月第一天是星期几

        // 添加前導空白天数，上個月的，灰色數字
        val daysList = mutableListOf<LocalDate>()
        for (i in 1 until firstDayOfWeek) {
            daysList.add(LocalDate.of(month.year, month.monthValue - 1, month.lengthOfMonth() - firstDayOfWeek + i + 1))
        }

        // 添加當前月的天数
        val daysInMonth = month.lengthOfMonth()
        for (dayOfMonth in 1..daysInMonth) {
            daysList.add(LocalDate.of(month.year, month.monthValue, dayOfMonth))
        }

        return daysList
    }
}


//@RequiresApi(Build.VERSION_CODES.O)
//@Preview(showBackground = true)
//@Composable
//fun CalendarPreview() {
//    CalendarMonthView(
//        displayMonth = LocalDate.now(),
//        onNextMonth = {},
//        onPreviousMonth = {}
//    )
//}