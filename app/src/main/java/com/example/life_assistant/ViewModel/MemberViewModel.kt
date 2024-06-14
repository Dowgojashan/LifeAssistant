package com.example.life_assistant.ViewModel

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.life_assistant.Event
import com.example.life_assistant.Screen.convertLongToDate
import com.example.life_assistant.data.Member
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MemberViewModel @Inject constructor(
    val auth: FirebaseAuth
): ViewModel(){
    val database = FirebaseDatabase.getInstance("https://life-assistant-27ae8-default-rtdb.europe-west1.firebasedatabase.app/")
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val registrationSuccess = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val member = mutableStateOf<Member?>(null)
    val email: MutableLiveData<String> = MutableLiveData()
    val showDialog = mutableStateOf(false)
    val dialogMessage = mutableStateOf("")

    //一開啟就檢查使用者是否登入
    init {
        checkUserStatus()
    }

    //檢查使用者是否登入
    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
    }

    //註冊
    fun onSignup(name:String, email: String, pass: String, birthday: Long) {
        inProgress.value = true

        // 將Long類型的日期轉換為日期字符串
        val formattedBirthday = convertLongToDate(birthday)

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userRef = database.getReference("members").child(userId)
                        userRef.apply {
                            child("name").setValue(name)
                            child("birthday").setValue(formattedBirthday).addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    registrationSuccess.value = true // 設置為 true，表示註冊成功
                                    Log.d("Registration", "viewmodel_registrationSuccess: ${registrationSuccess.value}")
                                } else {
                                    handleException(databaseTask.exception, "未能成功存入使用者資料")
                                }
                            }
                        }
                    }
                } else {
                    handleException(authTask.exception, "註冊失敗")
                }
                inProgress.value = false
            }
    }

    // 儲存習慣時間
    fun saveHabitTimes(wakeHour: Int, wakeMinute: Int, sleepHour: Int, sleepMinute: Int) {
        val memberId = auth.currentUser?.uid ?: return

        val habitRef = database.getReference("members").child(memberId).child("habits")
        val wakeTime = "$wakeHour:$wakeMinute"
        val sleepTime = "$sleepHour:$sleepMinute"

        val habits = mapOf(
            "wakeTime" to wakeTime,
            "sleepTime" to sleepTime
        )

        habitRef.setValue(habits).addOnSuccessListener {
            Log.d("Firebase", "Habit times saved successfully")
        }.addOnFailureListener { exception ->
            handleException(exception, "無法儲存使用習慣")
        }
    }


    //登入
    fun login(email: String, pass: String) {
        inProgress.value = true

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {authTask ->
                if (authTask.isSuccessful) {
                    signedIn.value = true
                    Log.d("AlertDialog", "sign: $signedIn.value ")
                    handleException(authTask.exception, "登入成功")
                } else {
                    handleException(authTask.exception, "登入失敗")
                }
                inProgress.value = false
            }
    }

    private val defaultErrorMessage = "發生未知錯誤，請稍後再試"
    val showSuccessDialog = mutableStateOf(false)
    val showErrorDialog = mutableStateOf(false)

    //忘記密碼
    fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 密碼重置電子郵件已發送
                    dialogMessage.value = "密碼重置電子郵件已發送！"
                    showSuccessDialog.value = true
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        val errorCode = exception.errorCode
                        val errorMessage = when (errorCode) {
                            "ERROR_INVALID_EMAIL" -> "電子郵件格式錯誤"
                            else -> "未知錯誤"
                        }
                        dialogMessage.value = errorMessage
                        showErrorDialog.value = true
                    } else {
                        // Handle other types of exceptions
                        dialogMessage.value = "未知錯誤"
                        showErrorDialog.value = true
                    }
                }
            }
        }

    //抓錯誤
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val formattedMessage = formatErrorMessage(exception, customMessage, errorMsg)
        errorMessage.value = formattedMessage
    }


    //抓取auth檢查後的錯誤
    private fun formatErrorMessage(exception: Exception?, customMessage: String, errorMsg: String): String {
        val defaultErrorMessage = if (customMessage.isEmpty()) {
            errorMsg
        } else {
            "$customMessage: $errorMsg"
        }

        if (exception is FirebaseAuthException) {
            return when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "電子郵件格式錯誤"
                "ERROR_WEAK_PASSWORD" -> "密碼至少大於等於六位"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "已經有相同的電子郵件被註冊"
                "ERROR_WRONG_PASSWORD" -> "密碼不正確"
                "ERROR_USER_NOT_FOUND" -> "帳號不存在"
                else -> defaultErrorMessage
            }
        }
        return defaultErrorMessage
    }

    //登出
    fun logout() {
        auth.signOut()
        signedIn.value = false
    }

    //錯誤訊息跳出提示框
    @Composable
    fun ErrorAlertDialog(
        showDialog: MutableState<Boolean>,
        message: String,
        onDismiss: () -> Unit
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {onDismiss()},
                title = {
                    Text(text = "提示")
                },
                text = {
                    Text(text = message)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            Log.d("AlertDialog", "Confirm button clicked, showDialog: $showDialog")
                            onDismiss()
                        }
                    ) {
                        Text("確定")
                    }
                }
            )
        }
    }

    //從資料庫取得資料
    fun getData() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("AlertDialog", "userid: $userId")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            email.value = currentUser.email
        } else {
            email.value = "No user signed in"
        }

        val memberRef = database.getReference("members").child(userId)
        memberRef.get().addOnSuccessListener { snapshot ->
            val memberData = snapshot.getValue(Member::class.java)
            member.value = memberData
        }.addOnFailureListener { exception ->
            handleException(exception, "無法獲取資料")
        }
    }

    //修改資料
    fun updateMemberData(newName: String) {
        val memberId = auth.currentUser?.uid ?: return

        val memberRef = database.getReference("members").child(memberId)
        memberRef.child("name").setValue(newName).addOnSuccessListener {
            showDialog.value = true
            getData()
        }.addOnFailureListener { exception ->
            handleException(exception, "無法更新資料")
        }
    }
}