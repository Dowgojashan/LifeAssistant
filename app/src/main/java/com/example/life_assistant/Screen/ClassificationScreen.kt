package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ClassificationScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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

            val colors = listOf(
                Color(0xffdb697a),
                Color(0xffee8575),
                Color(0xffffe9af),
                Color(0xff8dccb3),
                Color(0xff7fabd1),
                Color(0xff867bb9),
                Color(0xfff4d6d8)
            )

            data class ItemState(
                val label: String,
                var color: Color
            )

            val items = remember {
                mutableStateListOf(
                    ItemState("工作", colors[0]),
                    ItemState("娛樂", colors[1]),
                    ItemState("運動", colors[2]),
                    ItemState("生活雜務", colors[3]),
                    ItemState("讀書", colors[4]),
                    ItemState("旅遊", colors[5]),
                    ItemState("吃飯", colors[6])
                )
            }

            LazyColumn {
                items(items.size) { index ->
                    val item = items[index]
                    var isExpanded by remember { mutableStateOf(false) }
                    var isColorMenuExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .zIndex(0f)
                            .background(item.color, RoundedCornerShape(4.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    fontSize = 16.sp,
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
                                Box {
                                    IconButton(onClick = {
                                        isColorMenuExpanded = true
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.mdi_palette),
                                            contentDescription = "Change Color",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = isColorMenuExpanded,
                                        onDismissRequest = { isColorMenuExpanded = false }
                                    ) {
                                        colors.forEach { color ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    item.color = color
                                                    isColorMenuExpanded = false
                                                },
                                                text = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(color)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = {
                                    if (index > 0) {
                                        items.swap(index, index - 1)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.change_up),
                                        contentDescription = "Move Up",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    if (index < items.size - 1) {
                                        items.swap(index, index + 1)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.change_down),
                                        contentDescription = "Move Down",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                    Text(
                                        text = "詳細內容 ${item.label}",
                                        style = MaterialTheme.typography.bodyMedium
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

private fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    if (index1 == index2) return
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}
