// EventScreen.kt

package com.example.life_assistant.Screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.life_assistant.ViewModel.MemberViewModel


//@Composable
//fun EventScreen(navController: NavController, mvm: MemberViewModel) {
//    // 基本的事件屏幕顯示
//    Text(text = "這是行事曆頁面")
//}
@Composable
fun EventScreen(
    navController: NavController,
    mvm: MemberViewModel,
    modifier: Modifier = Modifier
) {

    val viewModel: MemberViewModel = viewModel()

    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val time = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("事件標題") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("事件描述") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = date.value,
            onValueChange = { date.value = it },
            label = { Text("事件日期") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = time.value,
            onValueChange = { time.value = it },
            label = { Text("事件時間") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.addEvent(title.value, description.value, date.value, time.value)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("新增事件")
        }
    }
}
