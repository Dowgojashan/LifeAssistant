package com.example.life_assistant.Screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel
import kotlinx.coroutines.launch

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
                            navController.navigate(DestinationScreen.MonthCalendar.route)
                        },
                        text = { Text("月行事曆") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            navController.navigate(DestinationScreen.Classification.route)
                        },
                        text = { Text("標籤分類") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            mvm.logout()
                        },
                        text = { Text("登出") }
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Classification",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = { /* Handle click */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.member),
                        contentDescription = "Next Week",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            val items = remember {
                mutableStateListOf(
                    "工作", "娛樂", "運動", "生活雜務", "讀書", "旅遊", "吃飯"
                )
            }

            val expandedStates = remember { mutableStateListOf(*Array(items.size) { false }) }
            val draggedItem = remember { mutableStateOf<Int?>(null) }
            val coroutineScope = rememberCoroutineScope()

            LazyColumn {
                items(items.size) { index ->
                    val item = items[index]
                    val isBeingDragged = draggedItem.value == index
                    val isExpanded = expandedStates[index]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .zIndex(if (isBeingDragged) 1f else 0f)
                            .background(
                                color = when (index) {
                                    0 -> Color(0xffd5e8f6).copy(alpha = 0.4f)
                                    1 -> Color(0xffb9e8e3).copy(alpha = 0.4f)
                                    2 -> Color(0xffcff8f3).copy(alpha = 0.4f)
                                    3 -> Color(0xffc8e0f0).copy(alpha = 0.4f)
                                    4 -> Color(0xffb4cfe2).copy(alpha = 0.4f)
                                    5 -> Color(0xffd5e8f6).copy(alpha = 0.4f)
                                    6 -> Color(0xffb9e8e3).copy(alpha = 0.4f)
                                    else -> Color(0xffcff8f3).copy(alpha = 0.4f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(16.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { draggedItem.value = index },
                                    onDragEnd = { draggedItem.value = null },
                                    onDragCancel = { draggedItem.value = null },
                                    onDrag = { change, _ ->
                                        change.consumeAllChanges()
                                        coroutineScope.launch {
                                            when {
                                                index > 0 && draggedItem.value != null -> {
                                                    items.move(index, index - 1)
                                                    draggedItem.value = index - 1
                                                }
                                                index < items.size - 1 && draggedItem.value != null -> {
                                                    items.move(index, index + 1)
                                                    draggedItem.value = index + 1
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicText(
                                    text = item,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    expandedStates[index] = !isExpanded
                                }) {
                                    Icon(
                                        painter = painterResource(id = if (isExpanded) R.drawable.up else R.drawable.down),
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                    // Add the detailed content for each item here
                                    Text(
                                        text = "詳細內容 $item",
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

private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) return
    val item = removeAt(fromIndex)
    add(toIndex, item)
}
