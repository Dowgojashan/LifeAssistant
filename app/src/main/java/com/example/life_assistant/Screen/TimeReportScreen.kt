package com.example.life_assistant.Screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.io.path.Path
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Path


@Composable
fun TimeReportScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false) }
    var selectedYearMonth by remember { mutableStateOf(getCurrentYearMonth()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white))
    ) {

        Column (
            modifier = modifier
                .offset(x=5.dp, y=15.dp)
        ){
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
                        navController.navigate(DestinationScreen.DailyCalendar.route)
                    },
                    text = {
                        Text("日行事曆")
                    }
                )

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
                        navController.navigate(DestinationScreen.Classification.route)
                    },
                    text = {
                        Text("標籤分類")
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
//                            androidx.compose.material3.DropdownMenuItem(
//                                onClick = {
//                                    expanded = false
//                                    navController.navigate(DestinationScreen.WeekCalendar.route)
//                                },
//                                text = {
//                                    androidx.compose.material3.Text("週行事曆")
//                                }
//                            )
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

        Text(
            text = "行程分析",
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 20.dp
                )
        )

        Image(
            painter = painterResource(id = R.drawable.sticky_notes),
            contentDescription = "note",
            modifier = Modifier
                .requiredSize(size = 50.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 10.dp)
        )

        Row(
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 60.dp
                )
        ){
            CustomDropdownMenu(
                onValueSelected = { value ->
                    selectedYearMonth = value // 更新選中的年份和月份
                }
            )
        }

        println("select:$selectedYearMonth")

        LaunchedEffect(Unit) {
            mvm.getColors()
        }
        LaunchedEffect(selectedYearMonth) {
            if (selectedYearMonth.length > 8) {
                // yyyy年M月D日格式
                mvm.getTotalTimeByTagForDay(selectedYearMonth)
            } else {
                // yyyy年M月格式
                mvm.getTotalTimeByTagForMonth(selectedYearMonth)
            }
            mvm.getColors()
        }

        val eventByTag by mvm.eventsByTag.observeAsState(emptyMap())
        println("event:$eventByTag")

        val colorTag = mvm.colors.value
        val initialReadingColors = colorTag?.readingColors ?:0xff7fabd1
        val initialWorkColors = colorTag?.workColors ?: 0xffdb697a
        val initialSportColors = colorTag?.sportColors ?: 0xffffe9af
        val initialLeisureColors = colorTag?.leisureColors ?: 0xffee8575
        val initialHouseworkColors = colorTag?.houseworkColors ?: 0xff8dccb3
        val initialTravelColors = colorTag?.travelColors ?: 0xff867bb9
        val initialEatingColors = colorTag?.eatingColors ?: 0xfff4d6d8

        var readingColors by remember{ mutableLongStateOf(initialReadingColors) }
        var workColors by remember{ mutableLongStateOf(initialWorkColors) }
        var sportColors by remember{ mutableLongStateOf(initialSportColors) }
        var leisureColors by remember{ mutableLongStateOf(initialLeisureColors) }
        var houseworkColors by remember{ mutableLongStateOf(initialHouseworkColors) }
        var travelColors by remember{ mutableLongStateOf(initialTravelColors) }
        var eatingColors by remember{ mutableLongStateOf(initialEatingColors) }

        LaunchedEffect(colorTag) {
            readingColors = colorTag?.readingColors ?: 0xff7fabd1
            workColors = colorTag?.workColors ?: 0xffdb697a
            sportColors = colorTag?.sportColors ?: 0xffffe9af
            leisureColors = colorTag?.leisureColors ?: 0xffee8575
            houseworkColors = colorTag?.houseworkColors ?: 0xff8dccb3
            travelColors = colorTag?.travelColors ?: 0xff867bb9
            eatingColors = colorTag?.eatingColors ?: 0xfff4d6d8
        }

        var workHours by remember{mutableFloatStateOf(0f)}
        var eatingHours by remember{mutableFloatStateOf(0f)}
        var houseworkHours by remember{mutableFloatStateOf(0f)}
        var leisureHours by remember{mutableFloatStateOf(0f)}
        var sportHours by remember{mutableFloatStateOf(0f)}
        var readingHours by remember{mutableFloatStateOf(0f)}
        var travelHours by remember{mutableFloatStateOf(0f)}

        val categories = listOf(
            Category("工作", workHours),
            Category("吃飯", eatingHours),
            Category("生活雜務", houseworkHours),
            Category("娛樂", leisureHours),
            Category("運動", sportHours),
            Category("讀書", readingHours),
            Category("旅遊", travelHours)
        )

        // 將事件的標籤時間更新到對應的 Category 中
        // 更新 Category 列表中的時間
        val updatedCategories = categories.map { category ->
            val timeOfCategory = eventByTag.get(category.name) ?: 0.0
            category.copy(hours = timeOfCategory.toFloat())
        }

        // 分別取出七個變數
        workHours = updatedCategories.find { it.name == "工作" }?.hours ?: 0f
        eatingHours = updatedCategories.find { it.name == "吃飯" }?.hours ?: 0f
        houseworkHours = updatedCategories.find { it.name == "生活雜務" }?.hours ?: 0f
        leisureHours = updatedCategories.find { it.name == "娛樂" }?.hours ?: 0f
        sportHours = updatedCategories.find { it.name == "運動" }?.hours ?: 0f
        readingHours = updatedCategories.find { it.name == "讀書" }?.hours ?: 0f
        travelHours = updatedCategories.find { it.name == "旅遊" }?.hours ?: 0f

        // 計算總時長
        val totalHours = categories.map { it.hours }.sum()

        Box(
            modifier = Modifier
                .size(175.dp)
                .offset(y = 175.dp)
                .align(alignment = Alignment.TopCenter) // Center the PieChart in the Box
        ) {
            PieChart(
                categories = categories,
                colors = listOf(
                    Color(workColors), //工作標籤的顏色
                    Color(eatingColors), //吃飯
                    Color(houseworkColors), //生活雜務
                    Color(leisureColors), //娛樂
                    Color(sportColors), //運動
                    Color(readingColors), //讀書
                    Color(travelColors), //旅遊
                ),
                modifier = Modifier
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(460.dp)
                .background(
                    color = colorResource(id = R.color.light_blue).copy(alpha = 0.7f),
                    shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp) // Rounded corners
                )
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "分類資訊",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        y = 20.dp
                    )
            )

            // 定義 LazyColumn 顯示各種類別及其所花費的時間和百分比
            LazyColumn(
                modifier = Modifier
                    .requiredWidth(300.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 50.dp)
            ) {
                items(categories) { category ->
                    val percentage = if (totalHours > 0) (category.hours / totalHours * 100).toFloat() else 0f
                    val percentageText = String.format("%.1f%%", percentage)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(color = Color.Transparent),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "${category.hours}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            textAlign = TextAlign.End, // 確保數字靠右對齊
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "小時",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start, // 確保 "小時" 靠左對齊
                            modifier = Modifier.wrapContentWidth()// 固定寬度，確保 "小時" 對齊
                        )

                        Text(
                            text = percentageText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            textAlign = TextAlign.End, // 確保百分比靠右對齊
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PieChart(
    categories: List<Category>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = categories.map { it.hours }.sum()
    if (total == 0f) {
        Log.e("PieChart", "Total hours are zero, cannot calculate proportions.")
        return
    }
    val proportions = categories.map { it.hours / total * 360f }.toMutableList()
    if (proportions.any { it.isNaN() }) {
        Log.e("PieChart", "Proportions contain NaN values, cannot proceed.")
        return
    }
    val nonZeroProportions = proportions.filter { it > 0 }
    if (nonZeroProportions.size == 1) {
        // Set the only non-zero proportion to 360f
        val index = proportions.indexOfFirst { it > 0 }
        proportions.fill(0f)
        proportions[index] = 360f
    }

    val event = categories.map{it.hours}
    println("proportions:$proportions")
    println("event:$event")
    var touchedIndex by remember { mutableStateOf<Int?>(null) }
    println("total:$total")

    Box(modifier = modifier.aspectRatio(1f)) {
        // aspectRatio(1f) 设置 Box 的宽高比为1:1, 确保 Box 内部的 Canvas 是正方形
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // pointerInput 处理手势输入
                detectTapGestures { offset ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val radius = minOf(canvasWidth, canvasHeight) / 2
                    val center = Offset((canvasWidth / 2).toFloat(), (canvasHeight / 2).toFloat())
                    val distanceToCenter = (offset - center).getDistance()

                    if (distanceToCenter > radius) {
                        touchedIndex = null
                        return@detectTapGestures
                    }

                    val angle = (atan2(
                        (offset.y - center.y).toDouble(),
                        (offset.x - center.x).toDouble()
                    ) * (180 / Math.PI)).toFloat()
                    val adjustedAngle = (angle + 360) % 360 // 將角度範圍調整為0~360度
                    println("angle:$adjustedAngle")

                    var accumulatedAngle = 0f
                    touchedIndex = proportions.indexOfFirst { proportion ->
                        Log.d("PieChart", "adjustedAngle: $adjustedAngle")
                        Log.d("PieChart", "accumulatedAngle before: $accumulatedAngle")
                        if (accumulatedAngle.isNaN() || proportion.isNaN()) {
                            Log.e("PieChart", "accumulatedAngle or proportion is NaN. Stopping calculation.")
                            return@indexOfFirst false
                        }
                        val inRange = adjustedAngle >= accumulatedAngle && adjustedAngle < accumulatedAngle + proportion
                        accumulatedAngle += proportion
                        Log.d("PieChart", "accumulatedAngle after: $accumulatedAngle")
                        inRange
                    }.takeIf { it >= 0 }

                }
            }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            val innerRadius = radius * 0.6f    // 内部半径，用于中空效果
            var startAngle = 0f

            // 遍历每个扇形区域
            for (i in proportions.indices) {
                val isTouched = touchedIndex == i
                val sweepAngle = proportions[i]

                // 应用悬浮效果
                val pathRadius = if (isTouched) radius * 1.1f else radius
                val path = Path().apply {
                    arcTo(
                        rect = Rect(center.x - pathRadius, center.y - pathRadius, center.x + pathRadius, center.y + pathRadius),
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = sweepAngle,
                        forceMoveTo = false
                    )
                    arcTo(
                        rect = Rect(center.x - innerRadius, center.y - innerRadius, center.x + innerRadius, center.y + innerRadius),
                        startAngleDegrees = startAngle + sweepAngle,
                        sweepAngleDegrees = -sweepAngle,
                        forceMoveTo = false
                    )
                    close()
                }
                println("startAngle:$startAngle")
                println("sweepangle:$sweepAngle")

                drawPath(
                    path = path,
                    color = colors[i]
                )

                startAngle += sweepAngle
            }


            // 如果有被点击的区域，绘制分类名称和时数占总时数的百分比在圆饼图中心
            val displayText = touchedIndex?.let { index ->
                if (index != -1) {
                    val category = categories[index]
                    val percentage = (category.hours / total * 100).toInt()
                    "${category.name}\n${percentage}%"
                } else {
                    "共\n$total h"
                }
            } ?: "共\n$total h"

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 20.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }

            // 计算文本的行高和总高度
            val fontMetrics = paint.fontMetrics
            val lineHeight = fontMetrics.descent - fontMetrics.ascent
            val textHeight = lineHeight * (displayText.count { it == '\n' } + 1)

            // 计算文本的绘制起始位置，确保文本垂直居中
            val textOffsetY = center.y - textHeight / 2 //- fontMetrics.descent

            displayText.split("\n").forEachIndexed { index, line ->
                drawContext.canvas.nativeCanvas.drawText(
                    line,
                    center.x,
                    textOffsetY + (lineHeight * index) - (fontMetrics.ascent),
                    paint
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(onValueSelected: (String) -> Unit) {
    val currentYear = LocalDate.now().year
    val currentMonth = DateTimeFormatter.ofPattern("M月").format(LocalDate.now()).toString()

    var expandedOption1 by remember { mutableStateOf(false) }
    var selectedOption1 by remember { mutableStateOf("月") }
    var selectedOption2 by remember { mutableStateOf("${currentYear}年${currentMonth}") }
    var showDialog by remember { mutableStateOf(false) }

    // List of years and months
    (2024..2050).map { it.toString() } // Example range of years
    val months = (1..12).map { month ->
        DateTimeFormatter.ofPattern("M月").format(LocalDate.of(2024, month, 1))
    }

    var selectedYear by remember { mutableIntStateOf(currentYear) } // Default year
    var selectedMonth by remember { mutableStateOf(currentMonth) } // Default month

    // Display a dialog to select year and month
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("選擇年份和月份") },
            text = {
                Column {
                    // Year Dropdown
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = { /* No-op */ },
                        readOnly = true,
                        label = { Text("年份") },
                        leadingIcon = {
                            Column {
                                IconButton(onClick = { selectedYear -= 1 }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "上一年"
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            Column {
                                IconButton(onClick = { selectedYear += 1 }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "下一年"
                                    )
                                }
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    // Month Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(top = 8.dp)
                    ) {
                        items(months) { month ->

                            val isSelected = month == selectedMonth
                            val isCurrentMonth = month == currentMonth

                            Text(
                                text = month,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable {
                                        selectedMonth = month
                                    }
                                    .border(
                                        BorderStroke(
                                            2.dp,
                                            if (isCurrentMonth) colorResource(id = R.color.light_blue)
                                            else if (isSelected) Color.Red else Color.Transparent
                                        )
                                    )
                                    .background(
                                        Color.Transparent
                                    )
                                    .padding(8.dp),
                                textAlign = TextAlign.Center,
                                color =  Color.Black
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedOption2 = "${selectedYear}年${selectedMonth}"
                    onValueSelected(selectedOption2)
                    showDialog = false
                }) {
                    Text("確定")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    var dateDialogController by  remember { mutableStateOf(false) }

    // 獲取當前日期
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)


    val currentDate = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }.timeInMillis
    }

    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate,
        yearRange = 1970..2050
    )

    // Display DatePicker directly
    if (dateDialogController) {
        DatePickerDialog(
            onDismissRequest = { dateDialogController = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedOption2 = convertMillisToDateString(dateState.selectedDateMillis!!)
                    dateDialogController = false
                    onValueSelected(selectedOption2)
                }) {
                    Text(text = "確認")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dateDialogController = false
                }) {
                    Text(text = "取消")
                }
            }
        ) {
            DatePicker(
                state = dateState
            )
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // First Dropdown
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentSize(Alignment.TopStart)
        ) {
            OutlinedTextField(
                value = selectedOption1,
                onValueChange = { /* No-op */ }, // 該字段是只讀的，不允許手動輸入
                readOnly = true,
                label = { Text("日/月") },
                trailingIcon = {
                    IconButton(onClick = { expandedOption1 = true }) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOption1)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(60.dp)
            )
            DropdownMenu(
                expanded = expandedOption1,
                onDismissRequest = { expandedOption1 = false },
                modifier = Modifier
                    .requiredWidth(70.dp)
            ) {
                DropdownMenuItem(
                    onClick = {
                        selectedOption1 = "月"
                        selectedOption2 = getCurrentYearMonth()
                        expandedOption1 = false
                    },
                    text = { Text("月") }
                )
                DropdownMenuItem(
                    onClick = {
                        selectedOption1 = "日"
                        selectedOption2 = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
                        onValueSelected(selectedOption2)
                        expandedOption1 = false
                    },
                    text = {
                        Text("日")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Second Dropdown (Modified to use AlertDialog)
        OutlinedTextField(
            value = selectedOption2,
            onValueChange = { /* No-op */ },
            readOnly = true,
            label = { Text("選擇日期") },
            trailingIcon = {
                IconButton(onClick = {
                    if (selectedOption1 == "日") dateDialogController = true else showDialog = true
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.schedule),
                        contentDescription = "Select Date",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
                .requiredWidth(200.dp)
                .requiredHeight(60.dp)
        )
    }
}

data class Category(
    val name: String,
    val hours: Float
)


fun getCurrentYearMonth(): String {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月"))
}

@Composable
fun CategoryItem(category: Category) {
    Row(
        modifier = Modifier
            //.fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${category.hours}小时",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}