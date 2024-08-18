package com.example.life_assistant.Screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpHabitScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    val wakeState = rememberTimePickerState(0, 0, true)
    val sleepState = rememberTimePickerState(0, 0, true)
    val (selectedOption, setSelectedOption) = remember { mutableStateOf("") }//先甘後苦那個
    var habit by remember { mutableStateOf("")}
    val preferences = listOf("讀書", "運動", "工作", "娛樂","生活雜務")// 可根據需要添加更多選項
    val morningCheckedStates = remember { mutableStateMapOf<String, Boolean>().apply { preferences.forEach { put(it, false) } } }
    val noonCheckedStates = remember { mutableStateMapOf<String, Boolean>().apply { preferences.forEach { put(it, false) } } }
    val nightCheckedStates = remember { mutableStateMapOf<String, Boolean>().apply { preferences.forEach { put(it, false) } } }
    val readingTag = getPreferenceTag("讀書", morningCheckedStates, noonCheckedStates, nightCheckedStates)
    val sportTag = getPreferenceTag("運動", morningCheckedStates, noonCheckedStates, nightCheckedStates)
    val workTag = getPreferenceTag("工作", morningCheckedStates, noonCheckedStates, nightCheckedStates)
    val leisureTag = getPreferenceTag("娛樂", morningCheckedStates, noonCheckedStates, nightCheckedStates)
    val houseworkTag = getPreferenceTag("生活雜務", morningCheckedStates, noonCheckedStates, nightCheckedStates)




    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        //頂部內容
        TextButton(
            onClick = { navController.navigate(DestinationScreen.SignUp.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .requiredSize(size = 36.dp)
                .offset(x = 20.dp, y = 20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "back",
                modifier = Modifier
                    .requiredSize(size = 36.dp)
            )
        }
        Text(
            text = "使用習慣",
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 60.dp)
        )

        Box(
            modifier = Modifier
                .padding(top = 100.dp, bottom = 270.dp) //確保上部和下部的空間
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "起床時間",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TimeInput(
                    state = wakeState,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                        timeSelectorSelectedContentColor = Color.Black,
                        timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                        timeSelectorUnselectedContentColor = Color.Black
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 6.dp)
                )

                Text(
                    text = "睡覺時間",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TimeInput(
                    state = sleepState,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                        timeSelectorSelectedContentColor = Color.Black,
                        timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                        timeSelectorUnselectedContentColor = Color.Black
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 6.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "你更喜歡哪種安排行程的方式？",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            setSelectedOption("先完成任務後再享受放鬆時間")
                            habit = "先苦後甜"
                        }) {
                            Text(text = "先苦後甜")
                        }

                        TextButton(onClick = {
                            setSelectedOption("先享受放鬆時間後再完成任務")
                            habit = "先甜後苦"
                        }) {
                            Text(text = "先甜後苦")
                        }
                    }

                    if (selectedOption.isNotEmpty()) {
                        Text(
                            text = "你選擇的是：$selectedOption",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "偏好選擇",
                        color = Color.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    preferences.forEach { preference ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .fillMaxWidth()
                        ) {
                            Text(text = preference, textAlign = TextAlign.Center)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = morningCheckedStates[preference] ?: false,
                                    onCheckedChange = { checked -> morningCheckedStates[preference] = checked },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                                )
                                Text(text = "上午", textAlign = TextAlign.Center)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = noonCheckedStates[preference] ?: false,
                                    onCheckedChange = { checked -> noonCheckedStates[preference] = checked },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                                )
                                Text(text = "下午", textAlign = TextAlign.Center)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = nightCheckedStates[preference] ?: false,
                                    onCheckedChange = { checked -> nightCheckedStates[preference] = checked },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                                )
                                Text(text = "晚上", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }


        //註冊按鈕
        Button(
            onClick = {
                mvm.saveHabitTimes(wakeState.hour, wakeState.minute, sleepState.hour, sleepState.minute,habit,readingTag,sportTag,workTag,leisureTag,houseworkTag)
                navController.navigate(DestinationScreen.Login.route) },
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = modifier
                .requiredWidth(width = 100.dp)
                .requiredHeight(height = 50.dp)
                .align(Alignment.TopCenter) //文字對齊
                .offset(
                    y = 480.dp
                )
        ){
            Text(text = "註冊")
        }

        Image( //讀書人圖片
            painter = painterResource(id = R.drawable.reading_man),
            contentDescription = "reading_man",
            modifier = modifier
                .requiredWidth(width = 374.dp)
                .requiredHeight(height = 312.dp)
                .offset(
                    y = 500.dp
                )
        )

    }
}

// 函式來獲取每個偏好的選擇時間段
fun getPreferenceTag(preference: String, morningCheckedStates: Map<String, Boolean>, noonCheckedStates: Map<String, Boolean>, nightCheckedStates: Map<String, Boolean>): String {
    val tags = mutableListOf<String>()

    if (morningCheckedStates[preference] == true) tags.add("上午")
    if (noonCheckedStates[preference] == true) tags.add("下午")
    if (nightCheckedStates[preference] == true) tags.add("晚上")

    return tags.joinToString(separator = ",")
}