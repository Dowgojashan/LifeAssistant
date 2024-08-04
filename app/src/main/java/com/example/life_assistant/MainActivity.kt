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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.life_assistant.Main.NotificationMessage
import com.example.life_assistant.Screen.ClassificationScreen
import com.example.life_assistant.Screen.DailyCalendarScreen
import com.example.life_assistant.Screen.ForgetPasswordScreen
import dagger.hilt.android.AndroidEntryPoint
import com.example.life_assistant.Screen.LoginScreen
import com.example.life_assistant.Screen.MainScreen
import com.example.life_assistant.Screen.MonthCalendarScreen
import com.example.life_assistant.Screen.SignUpHabitScreen
import com.example.life_assistant.Screen.SignUpScreen
import com.example.life_assistant.Screen.TimeReportScreen
import com.example.life_assistant.Screen.WeekCalendarScreen
import com.example.life_assistant.Screen.calendarScreen
import com.example.life_assistant.ViewModel.EventViewModel
import com.example.life_assistant.ViewModel.MemberViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
    //object DailyCalendar: DestinationScreen("dailycalendar")
    object DailyCalendar : DestinationScreen("daily_calendar_screen/{date}")
    object MonthCalendar: DestinationScreen("monthcalendar")
    object WeekCalendar: DestinationScreen("weekcalendar")
    object Classification: DestinationScreen("Classification")
    object TimeReport: DestinationScreen("timereport")
}

@Composable
fun AuthenticationApp(){
    val mvm = hiltViewModel<MemberViewModel>()
    val evm = hiltViewModel<EventViewModel>()
    val navController = rememberNavController()
    val start = remember{ mutableStateOf(DestinationScreen.Login.route) }

    val currentMonth = remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

    fun goToNextMonth() {
        currentMonth.value = currentMonth.value.plusMonths(1)
    }

    fun goToPreviousMonth() {
        currentMonth.value = currentMonth.value.minusMonths(1)
    }

    NotificationMessage(mvm)

    // Determine the initial start destination based on signed-in status
    LaunchedEffect(mvm.signedIn.value) {
        start.value = if (mvm.signedIn.value) {
            DestinationScreen.DailyCalendar.route
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
            calendarScreen(navController,evm,mvm)
        }

        composable("daily_calendar_screen/{date}") { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
            val date = try {
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: DateTimeParseException) {
                LocalDate.now() // 在日期解析失敗時使用當前日期作為回退
            }
            DailyCalendarScreen(
                navController = navController,
                evm = evm,
                mvm = mvm,
                date = date
            )
        }

        composable(DestinationScreen.MonthCalendar.route){
            MonthCalendarScreen(navController, evm, mvm)
        }
        composable(DestinationScreen.WeekCalendar.route){
            WeekCalendarScreen(navController, evm, mvm)
        }
        composable(DestinationScreen.Classification.route){
            ClassificationScreen(navController,mvm)
        }
        composable(DestinationScreen.TimeReport.route){
            TimeReportScreen(navController,mvm)
        }
    }
}