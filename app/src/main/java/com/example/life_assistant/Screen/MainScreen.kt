package com.example.life_assistant.Screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.ViewModel.MemberViewModel
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun MainScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mvm.getData()
    }

    val member = mvm.member.value
    val getEmail by mvm.email.observeAsState("")

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .requiredWidth(280.dp)
                .requiredHeight(40.dp)
                .align(Alignment.TopCenter)
                .offset(y = 150.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                val containerColor = Color(0xfffafca3)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("遊戲暱稱： ${member?.name ?: ""}") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .requiredWidth(280.dp)
                        .requiredHeight(60.dp),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Box(
            modifier = modifier
                .requiredWidth(280.dp)
                .requiredHeight(40.dp)
                .align(Alignment.TopCenter)
                .offset(y = 210.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                val containerColor = Color(0xfffafca3)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("電子郵件: $getEmail") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .requiredWidth(280.dp)
                        .requiredHeight(60.dp),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Box(
            modifier = modifier
                .requiredWidth(280.dp)
                .requiredHeight(40.dp)
                .align(Alignment.TopCenter)
                .offset(y = 330.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                val containerColor = Color(0xfffafca3)
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("生日： ${member?.birthday ?: ""}") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .requiredWidth(280.dp)
                        .requiredHeight(60.dp),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Box(
            modifier = modifier
                .requiredWidth(120.dp)
                .requiredHeight(60.dp)
                .align(Alignment.TopCenter)
                .offset(y = 460.dp)
        ) {
            OutlinedButton(
                onClick = {
                    mvm.updateMemberData(name)
                    success = true
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffafca3)),
                border = BorderStroke(1.dp, Color(0xff534e4e)),
                modifier = Modifier
                    .requiredWidth(100.dp)
                    .requiredHeight(50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "修改",
                color = Color.Black.copy(alpha = 0.25f),
                fontSize = 20.sp,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = modifier
                .requiredWidth(120.dp)
                .requiredHeight(60.dp)
                .align(Alignment.TopCenter)
                .offset(y = 540.dp)
        ) {
            OutlinedButton(
                onClick = {
                    mvm.logout()
                    navController.navigate(DestinationScreen.Login.route)
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffafca3)),
                border = BorderStroke(1.dp, Color(0xff534e4e)),
                modifier = Modifier
                    .requiredWidth(100.dp)
                    .requiredHeight(50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "登出",
                color = Color.Black.copy(alpha = 0.25f),
                fontSize = 20.sp,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 添加导航到行事历的按钮
        Box(
            modifier = modifier
                .requiredWidth(120.dp)
                .requiredHeight(60.dp)
                .align(Alignment.TopCenter)
                .offset(y = 620.dp) // 可以根据需要调整位置
        ) {
            OutlinedButton(
                onClick = {
                    navController.navigate(DestinationScreen.Event.route) // 导航到行事历
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffafca3)),
                border = BorderStroke(1.dp, Color(0xff534e4e)),
                modifier = Modifier
                    .requiredWidth(100.dp)
                    .requiredHeight(50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "行事曆",
                color = Color.Black.copy(alpha = 0.25f),
                fontSize = 20.sp,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = modifier
                .requiredWidth(width = 120.dp)
                .requiredHeight(height = 60.dp)
                .align(Alignment.TopCenter)
                .offset(y = 620.dp) // Adjust the offset as needed
        ) {
            OutlinedButton(
                onClick = {
                    navController.navigate(DestinationScreen.Calendar.route)
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffafca3)),
                border = BorderStroke(1.dp, Color(0xff534e4e)),
                modifier = Modifier
                    .requiredWidth(width = 100.dp)
                    .requiredHeight(height = 50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "日歷",
                color = Color.Black.copy(alpha = 0.25f),
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

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
