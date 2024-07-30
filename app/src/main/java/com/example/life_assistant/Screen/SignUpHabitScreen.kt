package com.example.life_assistant.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
            .verticalScroll(rememberScrollState())  //使介面可滾動
    ) {
        //回註冊頁面
        TextButton(
            onClick = {navController.navigate(DestinationScreen.SignUp.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = modifier
                .requiredSize(size = 36.dp)
                .offset( x = 20.dp, y = 20.dp)  //元素對齊
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),  //引入照片
                contentDescription = "back",
                modifier = Modifier
                    .requiredSize(size = 36.dp)
                    .fillMaxSize())
        }
        Image( //泡泡左
            painter = painterResource(id = R.drawable.bubble_1),
            contentDescription = "bubble_1",
            modifier = modifier
                .requiredWidth(width = 74.dp)
                .requiredHeight(height = 60.dp)
                .offset(x = 40.dp, y = 80.dp))

        Image( //泡泡右
            painter = painterResource(id = R.drawable.bubble_2),
            contentDescription = "bubble_2",
            modifier = modifier
                .requiredWidth(width = 99.dp)
                .requiredHeight(height = 78.dp)
                .offset(x = 260.dp, y = 30.dp))

        Text(
            text = "使用習慣",
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 60.dp
                ))

        Text(
            text = "起床時間",
            color = Color.Black,
            fontSize = 20.sp,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 150.dp
                ))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 190.dp
                )
        ) {
            TimeInput( //時間輸入框
                state = wakeState,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                    timeSelectorSelectedContentColor = Color.Black,
                    timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                    timeSelectorUnselectedContentColor = Color.Black
                ))
        }

        Text(
            text = "睡覺時間",
            color = Color.Black,
            fontSize = 20.sp,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 300.dp
                ))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 340.dp
                )
        ) {
            TimeInput(  //時間輸入框
                state = sleepState,
                colors = TimePickerDefaults.colors(
                    timeSelectorSelectedContainerColor = Color(0xffb4cfe2),
                    timeSelectorSelectedContentColor = Color.Black,
                    timeSelectorUnselectedContainerColor = Color(0xffb4cfe2),
                    timeSelectorUnselectedContentColor = Color.Black
                ))
        }

        //註冊按鈕
        Button(
            onClick = {
                mvm.saveHabitTimes(wakeState.hour, wakeState.minute, sleepState.hour, sleepState.minute)
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

//@Preview(widthDp = 360, heightDp = 800)
//@Composable
//fun SignUpHabitPreview() {
//    val navController = rememberNavController()
//    val auth = FirebaseAuth.getInstance()
//    val vm = remember { MemberViewModel(auth) }
//
//    SignUpHabitScreen(navController = navController, mvm = vm, modifier = Modifier)
//}