package com.example.life_assistant.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var checkpassword by remember { mutableStateOf("") }
    var birthday by remember { mutableLongStateOf(0L) } //回傳選擇的日期
    var errorName by remember { mutableStateOf(false) }
    var errorEmail by remember { mutableStateOf(false) }
    var errorPassword by remember { mutableStateOf(false) }
    var errorCheckPassword by remember { mutableStateOf(false) } //有無輸入確認密碼
    var errorCheck by remember { mutableStateOf(false) } //檢查密碼與確認密碼是否符合
    var errorBirthday by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val errorMessage = mvm.errorMessage.value

    //錯誤訊息跳出
    if (errorMessage != null) {
        showDialog.value = true
        mvm.ErrorAlertDialog(
            showDialog = showDialog,
            message = errorMessage,
            onDismiss = {
                showDialog.value = false
                mvm.errorMessage.value = null
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
            .verticalScroll(rememberScrollState())  //使介面可滾動
    ) {
        TextButton( //返回上一頁
            onClick = { },
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
            text = "基本資料",
            color = Color.Black,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = 60.dp
                ))

        //名稱填寫欄位
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 170.dp
                )

        ) {
            //檢查是否有輸入名稱
            if (errorName) {
                showDialog.value = true
                mvm.ErrorAlertDialog(
                    showDialog = showDialog,
                    message = "請輸入遊戲暱稱",
                    onDismiss = {
                        showDialog.value = false
                        errorName = false
                    }
                )
            }

            val containerColor = Color(0xffb4cfe2)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it},
                label = {Text("姓名", color = Color.Black)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White),
                modifier = Modifier
                    .requiredWidth(width = 210.dp)
                    .requiredHeight(height = 60.dp),
                shape = RoundedCornerShape(15.dp),

                )

        }

        //電子郵件
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 230.dp
                )

        ) {
            //檢查是否有輸入電子郵件
            if(errorEmail){
                showDialog.value = true
                mvm.ErrorAlertDialog(
                    showDialog = showDialog,
                    message = "請輸入電子郵件",
                    onDismiss = {
                        showDialog.value = false
                        errorEmail = false
                    }
                )
            }

            val containerColor = Color(0xffb4cfe2)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it},
                label = {Text("電子郵件", color = Color.Black)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White),
                modifier = Modifier
                    .requiredWidth(width = 210.dp)
                    .requiredHeight(height = 60.dp),
                shape = RoundedCornerShape(15.dp)
            )
        }

        //生日
        Row (
            modifier = Modifier
                .requiredWidth(width = 210.dp)
                .padding(1.dp, 8.dp)
                .align(Alignment.TopCenter)
                .offset(y = 260.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            var dateDialogController by  remember { mutableStateOf(false) }
            val currentDate = remember {
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2024)
                    set(Calendar.MONTH, 5)
                    set(Calendar.DAY_OF_MONTH, 13)
                }.timeInMillis
            }
            val dateState = rememberDatePickerState(
                initialSelectedDateMillis = currentDate,
                yearRange = 1990..2024
            )

            Button(
                onClick = { dateDialogController = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffb4cfe2), contentColor = Color.Black),
                shape = RoundedCornerShape(15.dp)) {
                Text(text = "選擇生日")
            }
            if (dateDialogController) {
                DatePickerDialog(
                    onDismissRequest = { dateDialogController = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if(dateState.selectedDateMillis != null){
                                birthday = dateState.selectedDateMillis!!
                            }
                            dateDialogController = false
                        }) {
                            Text(text = "確認")
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
                    DatePicker(
                        state = dateState
                    )
                }
            }

            Text(text = if (birthday != 0L) convertLongToDate(birthday) else "尚未選擇日期")

            //檢查是否有填寫生日
            if(errorBirthday){
                showDialog.value = true
                mvm.ErrorAlertDialog(
                    showDialog = showDialog,
                    message = "請選擇生日",
                    onDismiss = {
                        showDialog.value = false
                        errorBirthday = false
                    }
                )
            }
        }

        //密碼
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 350.dp
                )

        ) {
            //檢查是否有填寫密碼
            if(errorPassword){
                showDialog.value = true
                mvm.ErrorAlertDialog(
                    showDialog = showDialog,
                    message = "請輸入密碼",
                    onDismiss = {
                        showDialog.value = false
                        errorPassword = false
                    }
                )
            }

            val containerColor = Color(0xffb4cfe2)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it},
                label = {Text("密碼", color = Color.Black)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White),
                modifier = Modifier
                    .requiredWidth(width = 210.dp)
                    .requiredHeight(height = 60.dp),
                shape = RoundedCornerShape(15.dp)
            )

        }

        //確認密碼
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 410.dp
                )

        ) {
            //檢查密碼與確認密碼是否相同
            if(errorCheckPassword){
                showDialog.value = true
                mvm.ErrorAlertDialog(
                    showDialog = showDialog,
                    message = "請輸入確認密碼",
                    onDismiss = {
                        showDialog.value = false
                        errorCheckPassword = false
                    }
                )
            }

            val containerColor = Color(0xffb4cfe2)
            OutlinedTextField(
                value = checkpassword,
                onValueChange = { checkpassword = it},
                label = {Text("確認密碼", color = Color.Black)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = containerColor,
                    unfocusedContainerColor = containerColor,
                    disabledContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.White,
                    focusedBorderColor = Color.White),
                modifier = Modifier
                    .requiredWidth(width = 210.dp)
                    .requiredHeight(height = 60.dp),
                shape = RoundedCornerShape(15.dp)
            )

        }

        Button( //前往下一頁
            onClick = {
                //檢查每個欄位是否有被填寫，皆有的話就進入onSignup，確認都沒問題會進入下一頁
                if(name.isNotEmpty()){
                    if (email.isNotEmpty()) {
                        errorEmail = false
                        if (password.isNotEmpty()) {
                            errorPassword = false
                            if (checkpassword.isNotEmpty()) {
                                errorCheckPassword = false
                                if (password == checkpassword) {
                                    errorCheck = false
                                        if(birthday != 0L){
                                            errorBirthday = false
                                            CoroutineScope(Dispatchers.Main).launch {
                                                mvm.onSignup(name, email, password, birthday)
                                            }
                                        }
                                        else{
                                            errorBirthday = true
                                        }
                                }
                                else {
                                    errorCheckPassword = true
                                }
                            }
                            else {
                                errorCheck = true
                            }
                        }
                        else {
                            errorPassword = true
                        }
                    }
                    else {
                        errorEmail = true
                    }
                }
                else{
                    errorName = true
                }
            },
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = modifier
                .requiredWidth(width = 100.dp)
                .requiredHeight(height = 50.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 480.dp
                )
        ){
            Text(text = "繼續")
        }
        if (mvm.registrationSuccess.value) {
            navController.navigate(DestinationScreen.SignUpHabit.route)
        }
        mvm.registrationSuccess.value = false
    }
}


fun convertLongToDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, Locale.CHINESE)
    return format.format(date)
}


@Preview(widthDp = 360, heightDp = 800)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val vm = remember { MemberViewModel(auth) }

    SignUpScreen(navController = navController, mvm = vm, modifier = Modifier)
}