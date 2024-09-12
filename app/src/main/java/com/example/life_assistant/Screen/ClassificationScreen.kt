package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import com.example.life_assistant.ViewModel.EventViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ClassificationScreen(
    navController: NavController,
    mvm: MemberViewModel,
    evm: EventViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mvm.getColors()
        mvm.getTag()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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
                        text = { Text("月行事曆") }
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
                        text = { Text("登出") }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "標籤分類",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }



            // Use remember to manage tag labels and colors
            val totalTags = remember { mutableStateListOf(
                "工作",
                "娛樂",
                "運動",
                "生活雜務",
                "讀書",
                "旅遊",
                "吃飯") }

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
            var houseworkColors by remember{mutableLongStateOf(initialHouseworkColors) }
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
                Color(0xffdb697a), //工作的顏色
                Color(0xffee8575), //娛樂的顏色
                Color(0xffffe9af), //運動的顏色
                Color(0xff8dccb3), //生活雜務的顏色
                Color(0xff7fabd1), //讀書的顏色
                Color(0xff867bb9), //旅遊的顏色
                Color(0xfff4d6d8) //吃飯的顏色
            )

            val initialMorningChecked by remember {mutableStateOf(false)}
            val initialNoonChecked by remember {mutableStateOf(false)}
            val initialNightChecked by remember { mutableStateOf(false)}
            // 定義一個資料類來保存標籤的偏好選擇
            class TagPreferences {
                var morningChecked by mutableStateOf(initialMorningChecked)
                var noonChecked by mutableStateOf(initialNoonChecked)
                var nightChecked by mutableStateOf(initialNightChecked)
            }

            val tagCheckedStates = remember {
                mutableStateMapOf(
                    "讀書" to TagPreferences(),
                    "運動" to TagPreferences(),
                    "工作" to TagPreferences(),
                    "娛樂" to TagPreferences(),
                    "生活雜務" to TagPreferences()
                )
            }

            fun updatePreferencesFromStoredData(tag: String, storedData: String) {
                val timePeriods = storedData.split(",")
                val preferences = tagCheckedStates[tag] ?: return

                preferences.morningChecked = "上午" in timePeriods
                preferences.noonChecked = "下午" in timePeriods
                preferences.nightChecked = "晚上" in timePeriods
                println("preferences:$preferences")
            }

            val tagPreferences = mvm.tagPreferences.value
            println("tag:$tagPreferences")

            // 更新 UI 標籤偏好
            LaunchedEffect(tagPreferences) {
                tagPreferences.forEach { (tag, storedData) ->
                    updatePreferencesFromStoredData(tag, storedData)
                    println("test:$tag,$storedData")
                }
            }

            fun updatePreferences(tag: String, timePeriod: String, isChecked: Boolean) {
                val preferences = tagCheckedStates[tag] ?: return

                when (timePeriod) {
                    "上午" -> preferences.morningChecked = isChecked
                    "下午" -> preferences.noonChecked = isChecked
                    "晚上" -> preferences.nightChecked = isChecked
                }

                // 轉換為所需的格式
                val formatPreferences = { prefs: TagPreferences ->
                    val selectedTimes = mutableListOf<String>()
                    if (prefs.morningChecked) selectedTimes.add("上午")
                    if (prefs.noonChecked) selectedTimes.add("下午")
                    if (prefs.nightChecked) selectedTimes.add("晚上")
                    selectedTimes.joinToString(separator = ",")
                }

                // 更新後端，使用格式化後的字符串
                mvm.updateTags(
                    formatPreferences(tagCheckedStates["讀書"] ?: TagPreferences()),
                    formatPreferences(tagCheckedStates["運動"] ?: TagPreferences()),
                    formatPreferences(tagCheckedStates["工作"] ?: TagPreferences()),
                    formatPreferences(tagCheckedStates["娛樂"] ?: TagPreferences()),
                    formatPreferences(tagCheckedStates["生活雜務"] ?: TagPreferences())
                )
            }

            fun formatDateTime(dateTimeString: String): String {
                val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val formatterOutput = DateTimeFormatter.ofPattern("MM/dd HH:mm")
                val dateTime = LocalDateTime.parse(dateTimeString, formatterInput)
                return dateTime.format(formatterOutput)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(totalTags) { index, tag ->
                    var isExpanded by remember { mutableStateOf(false) }
                    var isSettingsMenuExpanded by remember { mutableStateOf(false) }

                    val backgroundColor = when (tag) {
                        "讀書" -> Color(readingColors)
                        "運動" -> Color(sportColors)
                        "工作" -> Color(workColors)
                        "娛樂" -> Color(leisureColors)
                        "生活雜務" -> Color(houseworkColors)
                        "旅遊" -> Color(travelColors)
                        "吃飯" -> Color(eatingColors)
                        else -> Color.Gray // 預設顏色
                    }

                    var events by remember { mutableStateOf<List<MemberViewModel.SimpleEvent>>(emptyList()) }
                    // 獲取事件列表
                    LaunchedEffect(tag,isExpanded) {
                        mvm.getEventByTag(tag) { fetchedEvents ->
                            events = fetchedEvents
                        }
                    }
                    println("tags:$tag")
                    println("events:$events")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 18.sp,
                                    modifier = Modifier.weight(0.7f)
                                )
                                IconButton(onClick = {
                                    isExpanded = !isExpanded
                                }) {
                                    Icon(
                                        painter = painterResource(id = if (isExpanded) R.drawable.up else R.drawable.down),
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    isSettingsMenuExpanded = true
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.edit),
                                        contentDescription = "Change Settings",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = isSettingsMenuExpanded,
                                onDismissRequest = { isSettingsMenuExpanded = false }
                            ) {
                                // Color selection
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "選擇顏色",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        colors.forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(color, CircleShape)
                                                    .clickable {
                                                        // 根據標籤更新對應的顏色
                                                        val colorLong = color
                                                            .toArgb()
                                                            .toLong()

                                                        // Update state based on the tag
                                                        when (tag) {
                                                            "讀書" -> readingColors = colorLong
                                                            "運動" -> sportColors = colorLong
                                                            "工作" -> workColors = colorLong
                                                            "娛樂" -> leisureColors = colorLong
                                                            "生活雜務" -> houseworkColors =
                                                                colorLong

                                                            "旅遊" -> travelColors = colorLong
                                                            "吃飯" -> eatingColors = colorLong
                                                        }
                                                        isSettingsMenuExpanded = false
                                                        mvm.updateColors(
                                                            readingColors,
                                                            sportColors,
                                                            workColors,
                                                            leisureColors,
                                                            houseworkColors,
                                                            travelColors,
                                                            eatingColors
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                }

                                // Preference selection, skip if tag is "吃飯" or "旅遊"
                                if (tag != "吃飯" && tag != "旅遊") {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "偏好選擇",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Text(text = "上午")
                                            Checkbox(
                                                checked = tagCheckedStates[tag]?.morningChecked ?: false,
                                                onCheckedChange = { checked ->
                                                    tagCheckedStates[tag]?.morningChecked = checked
                                                    updatePreferences(tag, "上午", checked)
                                                }
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Text(text = "下午")
                                            Checkbox(
                                                checked = tagCheckedStates[tag]?.noonChecked ?: false,
                                                onCheckedChange = { checked ->
                                                    tagCheckedStates[tag]?.noonChecked = checked
                                                    updatePreferences(tag, "下午", checked)
                                                }
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Text(text = "晚上")
                                            Checkbox(
                                                checked = tagCheckedStates[tag]?.nightChecked ?: false,
                                                onCheckedChange = { checked ->
                                                    tagCheckedStates[tag]?.nightChecked = checked
                                                    updatePreferences(tag, "晚上", checked)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                    // 將事件按照開始時間排序
                                    val sortedEvents = events.sortedBy { event ->
                                        LocalDateTime.parse(event.startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    }

                                    sortedEvents.forEach { event ->
                                        val initialCheck = event.isDone
                                        var isChecked by remember { mutableStateOf(initialCheck) }
                                        val formattedStartTime = formatDateTime(event.startTime)
                                        val formattedEndTime = formatDateTime(event.endTime)

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = {
                                                    isChecked = it
                                                    evm.updateIsDone(isChecked, event.uid)
                                                }
                                            )
                                            Text(
                                                text = "${event.name} ($formattedStartTime - $formattedEndTime)",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                                ),
                                                modifier = Modifier
                                                    .padding(start = 8.dp, bottom = 8.dp)  // 適當的間距
                                                    .fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


