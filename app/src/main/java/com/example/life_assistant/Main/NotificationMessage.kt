package com.example.life_assistant.Main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.life_assistant.ViewModel.MemberViewModel


@Composable
fun NotificationMessage(mvm: MemberViewModel){
    val notifState = mvm.popupNotification.value
    val notifMessage = notifState?.getContentOrNull()
    if(notifMessage != null){
        Toast.makeText(LocalContext.current, notifMessage,Toast.LENGTH_SHORT).show()
    }

}