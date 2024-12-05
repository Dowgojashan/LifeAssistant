package com.example.life_assistant.Screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
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
import kotlin.time.Duration.Companion.hours

@Composable
fun FinishReportScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedYearMonth by remember { mutableStateOf(getCurrentYearMonth()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.white))
    ) {

        Column(
            modifier = modifier
                .offset(x = 5.dp, y = 15.dp)
        ) {
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

        Text(
            text = "完成率分析",
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
        ) {
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
        val eventDoneByTag by mvm.tagCompletionRate.observeAsState(emptyMap())
        println("event:$eventByTag,$eventDoneByTag")

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

        val colors = listOf(
            Color(0xFFDB697A), // 工作
            Color(0xFFEE8575), // 吃飯
            Color(0xFFFFE9AF), // 生活雜務
            Color(0xFF8DCCB3), // 娛樂
            Color(0xFF7FABD1), // 運動
            Color(0xFF867BB9), // 讀書
            Color(0xFFF4D6D8)  // 旅遊
        )

        val categories = listOf(
            CompletionData("工作", 85f, 45f),
            CompletionData("吃飯", 100f, 41f),
            CompletionData("生活雜務", 64f, 60f),
            CompletionData("娛樂", 37f, 35f),
            CompletionData("運動", 72f, 26f),
            CompletionData("讀書", 42f, 20f),
            CompletionData("旅遊", 100f, 100f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x=10.dp, y = 170.dp)
        ) {
            // 長條圖區域
            CompletionBarChart(
                categories = categories.map { it.label }, // 提取分类名称
                completionRates = categories.associate { it.label to it.completedPercentage }, // 生成完成率的映射
                punctualityRates = categories.associate { it.label to it.onTimePercentage }, // 生成準時完成率的映射
                barColors = colors // 使用定义的颜色
            )
        }

        // 完成率資訊表
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(300.dp)
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
            // Header Row
            Row(
                modifier = Modifier
                    .requiredWidth(300.dp)
                    .padding(vertical = 1.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 50.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "項目",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "完成",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 24.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "準時率",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 24.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            // Categorized Data
            LazyColumn(
                modifier = Modifier
                    .requiredWidth(300.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 90.dp)
            ) {
                itemsIndexed(categories) { _, category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text="${category.completedPercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text="${category.onTimePercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 24.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

}


data class CompletionData(
    val label: String,
    val completedPercentage: Float,
    val onTimePercentage: Float
)

@Composable
fun CompletionBarChart(
    categories: List<String>,
    completionRates: Map<String, Float>,
    punctualityRates: Map<String, Float>, // 準時完成率
    barColors: List<Color> // 每個分類的顏色
) {
    val maxPercentage = 100f // 最大百分比
    val barWidth = 16.dp // 每條長條的寬度
    val spaceBetweenBars = 7.5.dp // 條之間的間隔
    val chartHeight = 250.dp // 整個長條圖高度

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 繪製百分比標尺
        Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
            val step = 20 // 每隔20%一條線
            val chartBottom = size.height
            val chartLeft = 40.dp.toPx() // 左側預留空間
            val chartWidth = size.width - chartLeft * 1/2

            // 畫背景標尺線和數字
            for (i in 0..maxPercentage.toInt() step step) {
                val y = chartBottom - (i / maxPercentage) * chartBottom
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(chartLeft, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$i%",
                    chartLeft - 10.dp.toPx(),
                    y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // 畫分類的長條
            val barSpacing = barWidth.toPx() + spaceBetweenBars.toPx()
            categories.forEachIndexed { index, category ->
                val barXStart = chartLeft + index * barSpacing * 2 // 每個分類左右各占一部分空間
                val barColor = barColors[index % barColors.size]

                // 完成率長條
                val completionHeight = (completionRates[category] ?: 0f) / maxPercentage * chartBottom
                drawRect(
                    color = barColor,
                    topLeft = Offset(barXStart, chartBottom - completionHeight),
                    size = Size(barWidth.toPx(), completionHeight)
                )

                // 準時完成率長條
                val punctualityHeight = (punctualityRates[category] ?: 0f) / maxPercentage * chartBottom
                drawRect(
                    color = barColor.copy(alpha = 0.5f),
                    topLeft = Offset(barXStart + barWidth.toPx(), chartBottom - punctualityHeight),
                    size = Size(barWidth.toPx(), punctualityHeight)
                )
            }
        }

        // 繪製分類名稱
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            categories.forEach {category ->
                Box(
                    modifier = Modifier
                        .width(barWidth * 1/2 + spaceBetweenBars ) // 与条形图组宽度对齐
                        .padding(top = 8.dp), // 分类名与条形图之间的间距
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.map { "$it\n" }.joinToString("").trim(), // 垂直显示
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        maxLines = category.length // 限制行数
                    )
                }
            }
        }
    }
}
