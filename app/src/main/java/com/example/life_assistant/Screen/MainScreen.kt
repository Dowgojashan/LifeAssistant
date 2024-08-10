package com.example.life_assistant.Screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    val member = mvm.member.value
    val getEmail by mvm.email.observeAsState("")

    //編輯前的原始資料
    val initialName = member?.name ?: ""
    val initialBirthday = member?.birthday ?: ""
    val initialSleepTime = member?.sleepTime ?: ""
    val initialWakeTime = member?.wakeTime ?: ""


// 使用 `rememberTimePickerState` 設置初始時間
    val initialWakeHour = initialWakeTime.split(":").getOrElse(0) { "0" }.toIntOrNull() ?: 0
    val initialWakeMinute = initialWakeTime.split(":").getOrElse(1) { "0" }.toIntOrNull() ?: 0
    val wakeState = rememberTimePickerState(initialWakeHour, initialWakeMinute, true)

    val initialSleepHour = initialSleepTime.split(":").getOrElse(0) { "0" }.toIntOrNull() ?: 0
    val initialSleepMinute = initialSleepTime.split(":").getOrElse(1) { "0" }.toIntOrNull() ?: 0
    val sleepState = rememberTimePickerState(initialSleepHour, initialSleepMinute, true)

// 使用 `remember` 記住狀態
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    var showDialoghint by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        mvm.getData()
    }

    LaunchedEffect(member, getEmail) {
        name = member?.name ?: ""
        email = getEmail
        birthday = member?.birthday ?: ""
        wakeTime = member?.wakeTime ?: ""
        sleepTime = member?.sleepTime ?: ""
    }

    Log.d("name",name)
    if (showDialoghint) {
        AlertDialog(
            onDismissRequest = { showDialoghint = false },
            title = { Text("提示") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("修改資料後，\n記得按下確認修改喔!")
            } },
            confirmButton = {
                TextButton(onClick = { showDialoghint = false }) {
                    Text("我瞭解了!")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.light_blue))
    ) {
        Column{
            IconButton(onClick = { expanded = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.change),
                    contentDescription = "More Options",
                    modifier = Modifier.size(32.dp)
                )
            }
            androidx.compose.material3.DropdownMenu(
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

        Image(
            painter = painterResource(id = R.drawable.lamp),
            contentDescription = "lamp",
            modifier = Modifier
                .requiredSize(size = 85.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp)
        )

        Text(
            text = "個人資料",
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 60.dp
                )
        )

        Box(
            modifier = Modifier
                .requiredWidth(310.dp)
                .requiredHeight(30.dp)
                .align(Alignment.TopCenter)
                .offset(y = 150.dp)
        ) {
            var showDialogName by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier.width(90.dp) // 固定寬度的Box
                ) {
                    Text(
                        text = "暱稱",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = modifier
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Log.d("test",name)
                Text(
                    text = name,
                    color = Color.Black,
                    fontSize =16.sp,
                    modifier = modifier
                )

                Spacer(modifier = Modifier.weight(1f))//占用剩餘空間

                IconButton(onClick = { showDialogName = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit name")
                }

                if (showDialogName) {
                    AlertDialog(
                        onDismissRequest = { showDialogName = false },
                        title = { Text(text = "修改暱稱") },
                        text = {
                            TextField(
                                value = member?.name ?: "",
                                onValueChange = { name = it },
                                placeholder = { Text(text = "輸入新暱稱") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                showDialogName = false

                            }) {
                                Text("暫存")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialogName = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .offset(y = 180.dp)
                .align(Alignment.TopCenter)
                .requiredWidth(310.dp)
        )

        Box(
            modifier = modifier
                .requiredWidth(width = 310.dp)
                .requiredHeight(height = 30.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 210.dp
                )

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ){
                Box(
                    modifier = Modifier.width(90.dp) // 固定寬度的Box
                ) {
                    Text(
                        text = "電子郵件",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Text(
                    text = getEmail,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = modifier

                )

                Spacer(modifier = Modifier.weight(1f))//占用剩餘空間
            }

        }

        Divider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .offset(y = 240.dp)
                .align(Alignment.TopCenter)
                .requiredWidth(310.dp)
        )

        Box(
            modifier = Modifier
                .requiredWidth(310.dp)
                .requiredHeight(30.dp)
                .align(Alignment.TopCenter)
                .offset(y = 270.dp)
        ) {
            var showDialogBirthday by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier.width(90.dp) // 固定寬度的Box
                ) {
                    Text(
                        text = "生日",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = modifier
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = birthday,
                    color = Color.Black,
                    fontSize =16.sp,
                    modifier = modifier
                )

                Spacer(modifier = Modifier.weight(1f))//占用剩餘空間

                IconButton(onClick = { showDialogBirthday = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit birthday")
                }

                if (showDialogBirthday) {
                    AlertDialog(
                        onDismissRequest = { showDialogBirthday = false },
                        title = { Text(text = "修改生日") },
                        text = {
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
                                yearRange = 1970..2024
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { dateDialogController = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffb4cfe2), contentColor = Color.Black),
                                    shape = RoundedCornerShape(15.dp)
                                ) {
                                    Text(text = "選擇生日")
                                }

                                if (dateDialogController) {
                                    DatePickerDialog(
                                        onDismissRequest = { dateDialogController = false },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                if (dateState.selectedDateMillis != null) {
                                                    birthday = convertMillisToDateString(dateState.selectedDateMillis!!)
                                                }
                                                dateDialogController = false
                                            }) {
                                                Text(text = "暫存")
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
                                        DatePicker(state = dateState)
                                    }
                                }

                                Text(
                                    text = if (birthday.isNotEmpty()) birthday else "尚未選擇日期",
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                showDialogBirthday = false
                                // TODO: handle nickname change
                            }) {
                                Text("暫存")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialogBirthday = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .offset(y = 300.dp)
                .align(Alignment.TopCenter)
                .requiredWidth(310.dp)
        )

        Box(
            modifier = modifier
                .requiredWidth(width = 310.dp)
                .requiredHeight(height = 30.dp)
                .align(Alignment.TopCenter)
                .offset(y = 330.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                var showDialogWakeupTime by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.width(90.dp) // 固定寬度的Box
                ) {
                    Text(
                        text = "起床時間",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = wakeTime,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = modifier
                )

                Spacer(modifier = Modifier.weight(1f)) //占用剩餘空間

                IconButton(onClick = { showDialogWakeupTime = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit wake-up time")
                }

                if (showDialogWakeupTime) {
                    AlertDialog(
                        onDismissRequest = { showDialogWakeupTime = false },
                        title = { Text(text = "修改起床時間") },
                        text = {
                            TimeInput( //時間輸入框
                                state = wakeState,
                                colors = TimePickerDefaults.colors(
                                    timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                                    timeSelectorSelectedContentColor = Color.Black,
                                    timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                                    timeSelectorUnselectedContentColor = Color.Black
                                )
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                // 獲取使用者選擇的時間
                                val selectedHour = wakeState.hour
                                val selectedMinute = wakeState.minute
                                // 格式化時間為 HH:mm
                                wakeTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                                showDialogWakeupTime = false
                            }) {
                                Text("暫存")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialogWakeupTime = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .offset(y = 360.dp)
                .align(Alignment.TopCenter)
                .requiredWidth(310.dp)
        )

        Box(
            modifier = modifier
                .requiredWidth(width = 310.dp)
                .requiredHeight(height = 30.dp)
                .align(Alignment.TopCenter)
                .offset(y = 390.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                var showDialogSleepTime by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.width(90.dp) // 固定寬度的Box
                ) {
                    Text(
                        text = "睡覺時間",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = sleepTime,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = modifier
                )

                Spacer(modifier = Modifier.weight(1f))//占用剩餘空間

                IconButton(onClick = { showDialogSleepTime = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit sleep time")
                }

                if (showDialogSleepTime) {
                    AlertDialog(
                        onDismissRequest = { showDialogSleepTime = false },
                        title = { Text(text = "修改睡覺時間") },
                        text = {
                            TimeInput( //時間輸入框
                                state = sleepState,
                                colors = TimePickerDefaults.colors(
                                    timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                                    timeSelectorSelectedContentColor = Color.Black,
                                    timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                                    timeSelectorUnselectedContentColor = Color.Black
                                )
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                // 獲取使用者選擇的時間
                                val selectedHour = sleepState.hour
                                val selectedMinute = sleepState.minute
                                // 格式化時間為 HH:mm
                                sleepTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                                showDialogSleepTime = false
                            }) {
                                Text("暫存")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialogSleepTime = false }) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .offset(y = 420.dp)
                .align(Alignment.TopCenter)
                .requiredWidth(310.dp)
        )

        Box(
            modifier = modifier
                .requiredWidth(width = 120.dp)
                .requiredHeight(height = 60.dp)
                .offset(
                    y = 450.dp
                )
                .align(Alignment.TopCenter)
        ) {
            Button(
                onClick = {
                    val finalName = if (name.isBlank()) initialName else name
                    val finalBirthday = if (birthday.isBlank()) initialBirthday else birthday
                    val finalSleepTime = if (sleepTime.isBlank()) initialSleepTime else sleepTime
                    val finalWakeTime = if (wakeTime.isBlank()) initialWakeTime else wakeTime

                    mvm.updateMemberData(finalName, finalBirthday, finalSleepTime, finalWakeTime)
                    success = true
                          },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue)),
                modifier = Modifier
                    .requiredWidth(width = 100.dp)
                    .requiredHeight(height = 50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "確認修改",
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 20.sp,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .align(Alignment.Center),
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = modifier
                .requiredWidth(width = 120.dp)
                .requiredHeight(height = 60.dp)
                .offset(
                    y = 450.dp
                )
                .align(Alignment.TopCenter)
        ) {
            Button(
                onClick = {
                    val finalName = if (name.isBlank()) initialName else name
                    val finalBirthday = if (birthday.isBlank()) initialBirthday else birthday
                    val finalSleepTime = if (sleepTime.isBlank()) initialSleepTime else sleepTime
                    val finalWakeTime = if (wakeTime.isBlank()) initialWakeTime else wakeTime

                    mvm.updateMemberData(finalName, finalBirthday, finalSleepTime, finalWakeTime)
                    success = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue)),
                modifier = Modifier
                    .requiredWidth(width = 100.dp)
                    .requiredHeight(height = 50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "確認修改",
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 20.sp,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .align(Alignment.Center),
                fontWeight = FontWeight.Bold
            )
        }

        Image(
            painter = painterResource(id = R.drawable.workplace),
            contentDescription = "lamp",
            modifier = Modifier
                .requiredSize(size = 180.dp)
                .align(Alignment.BottomCenter)
                .offset(y = -50.dp)
        )

        Log.d("AlertDialog", "Confirm button clicked, success: $success")
        if (success) {
            showDialog.value = true
            mvm.ErrorAlertDialog(
                showDialog = showDialog,
                message = "更改成功",
                onDismiss = {
                    showDialog.value = false
                    success = false
                }
            )
        }
    }
}

fun convertMillisToDateString(timeInMillis: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    return "${year}年${month}月${day}日"
}
