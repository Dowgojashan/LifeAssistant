package com.example.life_assistant.Screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
    var expanded by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mvm.getData()
    }

    val member = mvm.member.value
    val getEmail by mvm.email.observeAsState("")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.light_blue))
    ) {
        Column{
            androidx.compose.material3.IconButton(onClick = { expanded = true }) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.change),
                    contentDescription = "More Options",
                    modifier = Modifier.size(32.dp)
                )
            }
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    onClick = {
                        expanded = false
                        navController.navigate(DestinationScreen.DailyCalendar.route)
                    },
                    text = {
                        androidx.compose.material3.Text("日行事曆")
                    }
                )
                androidx.compose.material3.DropdownMenuItem(
                    onClick = {
                        expanded = false
                        navController.navigate(DestinationScreen.MonthCalendar.route)
                    },
                    text = {
                        androidx.compose.material3.Text("月行事曆")
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
                androidx.compose.material3.DropdownMenuItem(
                    onClick = {
                        expanded = false
                        mvm.logout()
                    },
                    text = {
                        androidx.compose.material3.Text("登出")
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
                .offset(x = -20.dp)
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
                .requiredWidth(280.dp)
                .requiredHeight(20.dp)
                .align(Alignment.TopCenter)
                .offset(y = 150.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                val containerColor = Color.Transparent
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {  Text("暱稱： ${member?.name ?: ""}",fontWeight = FontWeight.Bold) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .requiredWidth(280.dp)
                        .requiredHeight(60.dp)
                )
            }
        }

        Box(
            modifier = modifier
                .requiredWidth(width = 280.dp)
                .requiredHeight(height = 40.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 210.dp
                )

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ){
                val containerColor = Color.Transparent
                TextField(
                    value = email,
                    onValueChange = {email = it},
                    label = {Text ("電子郵件: $getEmail",fontWeight = FontWeight.Bold) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent),
                    modifier = Modifier
                        .requiredWidth(width = 280.dp)
                        .requiredHeight(height = 60.dp)
                )
            }

        }

        Box(
            modifier = modifier
                .requiredWidth(width = 280.dp)
                .requiredHeight(height = 40.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 270.dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                val containerColor = Color.Transparent
                TextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = {  Text("生日： ${member?.birthday ?: ""}",fontWeight = FontWeight.Bold) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .requiredWidth(280.dp)
                        .requiredHeight(60.dp),
                    singleLine = true
                )
            }
        }

        Column(
            modifier = modifier
                .requiredWidth(width = 280.dp)
                .requiredHeight(height = 20.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 330.dp
                )

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ){
                val containerColor = Color.Transparent
                var text by remember { mutableStateOf("") }
                TextField(
                    value = text,//email
                    onValueChange = { text = it}, //email=it
                    label = {Text ("起床時間： ",fontWeight = FontWeight.Bold) }, //$getEmail
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent),
                    modifier = Modifier
                        .requiredWidth(width = 280.dp)
                        .requiredHeight(height = 60.dp)
                )

            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(
                        y = 50.dp
                    )
                //.requiredHeight(height = 50.dp)
            ){
                val containerColor = Color.Transparent
                var text by remember { mutableStateOf("") }
                TextField(
                    value = text,//email
                    onValueChange = { text = it}, //email=it
                    label = {Text ("睡覺時間： ",fontWeight = FontWeight.Bold) }, //$getEmail
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = Color.Transparent),
                    modifier = Modifier
                        .requiredWidth(width = 280.dp)
                        .requiredHeight(height = 60.dp)
                )

            }
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
                    mvm.updateMemberData(name)
                    success = true },
                //shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.light_blue)),
                //border = BorderStroke(1.dp, Color(0xff534e4e)),
                modifier = Modifier
                    .requiredWidth(width = 100.dp)
                    .requiredHeight(height = 50.dp)
                    .align(Alignment.Center)
            ) {}
            Text(
                text = "修改",
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