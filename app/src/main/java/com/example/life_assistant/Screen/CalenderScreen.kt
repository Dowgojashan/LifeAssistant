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

@Composable
fun CalendarScreen(
    navController: NavHostController,
    mvm: MemberViewModel
) {
    val daysInMonth = 30
    val columns = 7
    var selectedDate by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(false) }

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
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .clickable {
                                            selectedDate = date
                                            showDialog = true
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
        NewEventDialog(date = selectedDate, onDismiss = { showDialog = false }, mvm)
    }
}

@Composable
fun NewEventDialog(date: Int, onDismiss: () -> Unit, mvm: MemberViewModel) {
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color.White, // Surface的颜色
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Add Event for $date", style = MaterialTheme.typography.headlineMedium)
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
                        // 这里可以添加逻辑保存事件到 ViewModel
                        mvm.addEvent(date, eventName, eventDescription)
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
