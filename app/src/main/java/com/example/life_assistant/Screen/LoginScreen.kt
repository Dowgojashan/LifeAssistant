package com.example.life_assistant.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.R
import com.example.life_assistant.ViewModel.MemberViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorEmail by remember { mutableStateOf(false) }
    var errorPassword by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) } //看密碼的可見性
    val showDialog = remember { mutableStateOf(false) }
    val signedIn by mvm.signedIn
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

    //檢查登入狀態，若為登入就進入主畫面
    if (signedIn) {
        navController.navigate(DestinationScreen.Main.route)
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
            .verticalScroll(rememberScrollState()) //使介面可滾動
    ) {
        Image(//趴著的小人
            painter = painterResource(id = R.drawable.laying_doodle),
            contentDescription = "laying_doodle",
            modifier = Modifier
                .requiredWidth(width = 374.dp)
                .requiredHeight(height = 312.dp)
                .offset(y = 100.dp)
                .rotate(degrees = -3.54f)
        )
        Image(//藍色區塊
            painter = painterResource(id = R.drawable.blue_rectangle),
            contentDescription = "blue_rectangle",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 230.dp)
                .fillMaxWidth()
                .requiredHeight(height = 680.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
        )
        Text(//登入標題
            text = "登入",
            color = Color.Black,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(y= 300.dp)
        )

        //帳號框框
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 400.dp
                )

        ){
            val containerColor = Color.White
            //檢查是否輸入電子郵件
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
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = {Text("帳號", color = Color.Black.copy(alpha = 0.31f))},
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
                singleLine = true
            )
        }

        //密碼框框
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 480.dp
                )

        ){
            val containerColor = Color.White
            //檢查是否輸入密碼
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
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                label = {Text("密碼", color = Color.Black.copy(alpha = 0.31f))},
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                    IconButton(onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(54.dp))
                    {
                        Icon(painter = painterResource(id = image), contentDescription = null)
                    }
                },
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
                singleLine = true
            )
        }

        //登入按鈕
        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    errorEmail = false
                    if (password.isNotEmpty()) {
                        errorPassword = false
                        mvm.login(email, password)
                    } else {
                        errorPassword = true
                    }
                }
                else {
                    errorEmail = true
                }
            },
            shape = RoundedCornerShape(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .requiredWidth(width = 100.dp)
                .requiredHeight(height = 50.dp)
                .align(Alignment.TopCenter)
                .offset(
                    x = 120.dp, y = 540.dp
                )
        ){Text(text = "登入",color = Color.White)
            if(mvm.signedIn.value){
                navController.navigate(DestinationScreen.Main.route)
            }}

        //尚未註冊按鈕
        Button(
            onClick = {navController.navigate(DestinationScreen.SignUp.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 0.5.dp, y = 221.5.dp)
        ) {Text(text = "註冊", color = Color.Black)}//註冊按鈕

        //忘記密碼按鈕
        Button(
            onClick = {navController.navigate(DestinationScreen.ForgetPassword.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 8.dp, y = 248.5.dp)
        ) {Text(text = "忘記密碼？", color = Color.Black)}

    }
}

//@Preview(widthDp = 360, heightDp = 800)
//@Composable
//fun LoginPreview() {
//    val navController = rememberNavController()
//    val auth = FirebaseAuth.getInstance()
//    val vm = remember { MemberViewModel(auth) }
//
//    LoginScreen(navController = navController, mvm = vm, modifier = Modifier)
//}