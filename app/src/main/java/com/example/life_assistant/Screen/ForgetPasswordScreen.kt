package com.example.life_assistant.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.life_assistant.DestinationScreen
import com.example.life_assistant.ViewModel.MemberViewModel
import com.example.life_assistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var errorEmail by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
            .verticalScroll(rememberScrollState())
    ){
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
        Text(//忘記密碼標題
            text = "請輸入電子郵件",
            color = Color.Black,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .align(Alignment.TopCenter)
                .offset(y= 300.dp)
        )

        //電子郵件框框
        Box(
            modifier = modifier
                .requiredWidth(width = 210.dp)
                .requiredHeight(height = 0.dp)
                .align(Alignment.TopCenter)
                .offset(
                    y = 420.dp
                )

        ){
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

            val containerColor = Color.White
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = {Text("email", color = Color.Black.copy(alpha = 0.31f))},
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

        //提交按鈕
        Button(
            onClick = {
                if(email.isNotEmpty()){
                    errorEmail = false
                    mvm.sendPasswordResetEmail(email)
                } else{
                    errorEmail = true
                }},
            shape = RoundedCornerShape(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .requiredWidth(width = 100.dp)
                .requiredHeight(height = 50.dp)
                .align(Alignment.TopCenter)
                .offset(
                    x = 120.dp, y = 500.dp
                )
        ){Text(text = "提交",color = Color.White)}

        //回到登入畫面
        Button(
            onClick = {navController.navigate(DestinationScreen.Login.route)},
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 0.5.dp, y = 248.5.dp)
        ) {Text(text = "回到登入畫面", color = Color.Black)}
    }

    // 顯示成功訊息對話框
    if (mvm.showSuccessDialog.value) {
        mvm.ErrorAlertDialog(
            showDialog = mvm.showSuccessDialog,
            message = mvm.dialogMessage.value,
            onDismiss = { mvm.showSuccessDialog.value = false }
        )
    }

    // 顯示錯誤訊息對話框
    if (mvm.showErrorDialog.value) {
        mvm.ErrorAlertDialog(
            showDialog = mvm.showErrorDialog,
            message = mvm.dialogMessage.value,
            onDismiss = { mvm.showErrorDialog.value = false }
        )
    }
}

//@Preview(widthDp = 360, heightDp = 800)
//@Composable
//fun RegisterPreview() {
//    val navController = rememberNavController()
//    val auth = FirebaseAuth.getInstance()
//    val vm = remember { MemberViewModel(auth) }
//
//    ForgetPassword(navController = navController, mvm = vm, modifier = Modifier)
//}