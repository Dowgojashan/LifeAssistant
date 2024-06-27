package com.example.life_assistant


import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.life_assistant.ui.theme.Life_AssistantTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.life_assistant.Main.NotificationMessage
import com.example.life_assistant.Screen.ForgetPasswordScreen
import dagger.hilt.android.AndroidEntryPoint
import com.example.life_assistant.Screen.LoginScreen
import com.example.life_assistant.Screen.MainScreen
import com.example.life_assistant.Screen.SignUpHabitScreen
import com.example.life_assistant.Screen.SignUpScreen
import com.example.life_assistant.Screen.CalendarScreen
import com.example.life_assistant.ViewModel.MemberViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Life_AssistantTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthenticationApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String){
    object Login: DestinationScreen("login")
    object SignUp: DestinationScreen("signup")
    object Main: DestinationScreen("main")
    object SignUpHabit: DestinationScreen("signuphabit")
    object ForgetPassword: DestinationScreen("forgetpassword")
    object Calendar: DestinationScreen("calendar")
}

@Composable
fun AuthenticationApp(){
    val mvm = hiltViewModel<MemberViewModel>()
    val navController = rememberNavController()
    val start = remember{ mutableStateOf(DestinationScreen.Login.route) }

    NotificationMessage(mvm)

    // Determine the initial start destination based on signed-in status
    LaunchedEffect(mvm.signedIn.value) {
        start.value = if (mvm.signedIn.value) {
            DestinationScreen.Calendar.route
        } else {
            DestinationScreen.Login.route
        }
    }

    NavHost(navController = navController, startDestination = start.value){
        composable(DestinationScreen.Login.route){
            LoginScreen(navController,mvm)
        }
        composable(DestinationScreen.Main.route){
            MainScreen(navController,mvm)
        }
        composable(DestinationScreen.SignUp.route){
            SignUpScreen(navController,mvm)
        }
        composable(DestinationScreen.SignUpHabit.route){
            SignUpHabitScreen(navController,mvm)
        }
        composable(DestinationScreen.ForgetPassword.route){
            ForgetPasswordScreen(navController,mvm)
        }
        composable(DestinationScreen.Calendar.route){
            CalendarScreen(navController,mvm)
        }
    }
}