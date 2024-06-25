// MemberViewModel.kt

package com.example.life_assistant.ViewModel

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.life_assistant.data.Event
import com.example.life_assistant.Screen.convertLongToDate
import com.example.life_assistant.data.Member
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MemberViewModel @Inject constructor(
<<<<<<< Updated upstream
    val auth: FirebaseAuth
): ViewModel() {
=======
    val auth: FirebaseAuth,
    private val memberRepository: MemberRepository
): ViewModel() {

>>>>>>> Stashed changes
    val database = FirebaseDatabase.getInstance("https://life-assistant-27ae8-default-rtdb.europe-west1.firebasedatabase.app/")
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<com.example.life_assistant.Event<String>?>(null)
    val registrationSuccess = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val member = mutableStateOf<Member?>(null)
    val email: MutableLiveData<String> = MutableLiveData()
    val showDialog = mutableStateOf(false)
    val dialogMessage = mutableStateOf("")

<<<<<<< Updated upstream
    // 一開啟就檢查使用者是否登入
=======
    // 添加日历事件相关的状态
    val events = mutableStateListOf<CalendarEvent>()

    // 初始化时检查用户状态
>>>>>>> Stashed changes
    init {
        checkUserStatus()
    }

<<<<<<< Updated upstream
    // 檢查使用者是否登入
=======
    // 检查用户是否已登录
>>>>>>> Stashed changes
    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
    }

<<<<<<< Updated upstream
<<<<<<< Updated upstream
    //註冊
=======
    // 註冊
>>>>>>> Stashed changes
=======
    // 添加事件
    fun addEvent(date: Int, name: String, description: String) {
        val memberId = auth.currentUser?.uid ?: return
        val event = CalendarEvent(date, name, description)
        events.add(event)

        // 将事件保存到 Firebase
        val eventRef = database.getReference("members").child(memberId).child("events").push()
        eventRef.setValue(event).addOnSuccessListener {
            Log.d("Firebase", "Event saved successfully")
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to save event")
        }
    }

    // 获取所有事件
    fun fetchEvents() {
        val memberId = auth.currentUser?.uid ?: return
        val eventRef = database.getReference("members").child(memberId).child("events")

        eventRef.get().addOnSuccessListener { snapshot ->
            events.clear()
            for (data in snapshot.children) {
                val event = data.getValue(CalendarEvent::class.java)
                event?.let {
                    events.add(it)
                }
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to fetch events")
        }
    }

    // 注册
>>>>>>> Stashed changes
    fun onSignup(name: String, email: String, pass: String, birthday: Long) {
        inProgress.value = true
        val formattedBirthday = convertLongToDate(birthday)

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val registrationRef = database.getReference("members").child(userId)
                        val memberData = mapOf(
                            "name" to name,
                            "birthday" to formattedBirthday
                        )
                        registrationRef.setValue(memberData).addOnCompleteListener { databaseTask ->
                            if (databaseTask.isSuccessful) {
                                val userRef = database.getReference("members").child(userId)
                                userRef.setValue(memberData).addOnCompleteListener { moveTask ->
                                    if (moveTask.isSuccessful) {
<<<<<<< Updated upstream
=======
                                        val memberEntity = MemberEntity(
                                            uid = userId,
                                            name = name,
                                            birthday = formattedBirthday
                                        )
                                        insertMember(memberEntity)
>>>>>>> Stashed changes
                                        registrationSuccess.value = true
                                        Log.d("Registration", "viewmodel_registrationSuccess: ${registrationSuccess.value}")
                                    } else {
                                        handleException(moveTask.exception, "Failed to save user data")
                                    }
                                }
                            } else {
                                handleException(databaseTask.exception, "Failed to save registration data")
                            }
                        }
                    }
                } else {
                    handleException(authTask.exception, "Registration failed")
                }
                inProgress.value = false
            }
    }

<<<<<<< Updated upstream
    // 儲存習慣時間
=======
    // 插入会员数据到本地数据库
    fun insertMember(memberEntity: MemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            memberRepository.insert(memberEntity)
        }
    }

    // 保存习惯时间
>>>>>>> Stashed changes
    fun saveHabitTimes(wakeHour: Int, wakeMinute: Int, sleepHour: Int, sleepMinute: Int) {
        val memberId = auth.currentUser?.uid ?: return
        val habitRef = database.getReference("members").child(memberId).child("habits")
        val wakeTime = String.format("%02d:%02d", wakeHour, wakeMinute)
        val sleepTime = String.format("%02d:%02d", sleepHour, sleepMinute)
        val habits = mapOf(
            "wakeTime" to wakeTime,
            "sleepTime" to sleepTime
        )

        habitRef.setValue(habits).addOnSuccessListener {
            Log.d("Firebase", "Habit times saved successfully")
<<<<<<< Updated upstream
=======
            viewModelScope.launch(Dispatchers.IO) {
                val existingMember = memberRepository.getMemberByUid(memberId)
                if (existingMember != null) {
                    val updatedMember = existingMember.copy(
                        wake_time = wakeTime,
                        sleep_time = sleepTime
                    )
                    memberRepository.update(updatedMember)
                }
            }
>>>>>>> Stashed changes
            auth.signOut()
            signedIn.value = false
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to save habit times")
        }
    }

<<<<<<< Updated upstream
    // 登入
=======
    // 登录
>>>>>>> Stashed changes
    fun login(email: String, pass: String) {
        inProgress.value = true

        auth.signInWithEmailAndPassword(email, pass)
<<<<<<< Updated upstream
<<<<<<< Updated upstream
            .addOnCompleteListener {authTask ->
=======
            .addOnCompleteListener { authTask ->
>>>>>>> Stashed changes
=======
            .addOnCompleteListener { authTask ->
>>>>>>> Stashed changes
                if (authTask.isSuccessful) {
                    signedIn.value = true
                    Log.d("AlertDialog", "sign: ${signedIn.value}")
                } else {
                    handleException(authTask.exception, "Login failed")
                }
                inProgress.value = false
            }
    }

    private val defaultErrorMessage = "發生未知錯誤，請稍後再試"
    val showSuccessDialog = mutableStateOf(false)
    val showErrorDialog = mutableStateOf(false)
<<<<<<< Updated upstream

    // 忘記密碼
=======
    // 忘记密码
>>>>>>> Stashed changes
    fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dialogMessage.value = "Password reset email sent!"
                    showSuccessDialog.value = true
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        val errorCode = exception.errorCode
                        val errorMessage = when (errorCode) {
                            "ERROR_INVALID_EMAIL" -> "Invalid email format"
                            else -> "Unknown error"
                        }
                        dialogMessage.value = errorMessage
                        showErrorDialog.value = true
                    } else {
                        dialogMessage.value = "Unknown error"
                        showErrorDialog.value = true
                    }
                }
            }
    }

<<<<<<< Updated upstream
    // 抓錯誤
=======
    // 捕获异常
>>>>>>> Stashed changes
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val formattedMessage = formatErrorMessage(exception, customMessage, errorMsg)
        errorMessage.value = formattedMessage
    }

<<<<<<< Updated upstream
    // 抓取auth檢查後的錯誤
=======
    // 格式化错误消息
>>>>>>> Stashed changes
    private fun formatErrorMessage(exception: Exception?, customMessage: String, errorMsg: String): String {
        val defaultErrorMessage = if (customMessage.isEmpty()) {
            errorMsg
        } else {
            "$customMessage: $errorMsg"
        }

        if (exception is FirebaseAuthException) {
            Log.d("AuthException", "Error Code: ${exception.errorCode}")
            return when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email format"
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                "ERROR_INVALID_CREDENTIAL" -> "Invalid email or password"
                else -> defaultErrorMessage
            }
        }
        return defaultErrorMessage
    }

    // 登出
    fun logout() {
        auth.signOut()
        signedIn.value = false
    }

<<<<<<< Updated upstream
    // 錯誤訊息跳出提示框
=======
    // 显示错误对话框
>>>>>>> Stashed changes
    @Composable
    fun ErrorAlertDialog(
        showDialog: MutableState<Boolean>,
        message: String,
        onDismiss: () -> Unit
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    Text(text = "Error")
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
                        Text("OK")
                    }
                }
            )
        }
    }

<<<<<<< Updated upstream
    // 從資料庫取得資料
=======
    // 获取用户数据
>>>>>>> Stashed changes
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
            handleException(exception, "Unable to fetch data")
        }
    }

<<<<<<< Updated upstream
    // 修改資料
=======
    // 更新用户数据
>>>>>>> Stashed changes
    fun updateMemberData(newName: String) {
        val memberId = auth.currentUser?.uid ?: return

        val memberRef = database.getReference("members").child(memberId)
        memberRef.child("name").setValue(newName).addOnSuccessListener {
            showDialog.value = true
<<<<<<< Updated upstream
            getData()
=======
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val currentMember = memberRepository.getMemberByUid(memberId)
                    if (currentMember != null) {
                        val updatedMember = currentMember.copy(name = newName)
                        memberRepository.update(updatedMember)
                        withContext(Dispatchers.Main) {
                            showDialog.value = true
                            getData()
                        }
                    } else {
                        Log.d("AlertDialog", "No member data found")
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        handleException(exception, "Unable to update data")
                    }
                }
            }
>>>>>>> Stashed changes
        }.addOnFailureListener { exception ->
            handleException(exception, "Unable to update data")
        }
    }
<<<<<<< Updated upstream

    // 新增事件
    fun addEvent(title: String, description: String, date: String, time: String) {
        val userId = auth.currentUser?.uid ?: return

        val eventRef = database.getReference("events").child(userId).push()
        val event = Event(title, description, date, time)

        eventRef.setValue(event).addOnSuccessListener {
            Log.d("Firebase", "Event saved successfully")
        }.addOnFailureListener { exception ->
            handleException(exception, "無法儲存事件")
        }
    }
}
=======
}

// 日历事件数据类
data class CalendarEvent(
    val date: Int = 0,
    val name: String = "",
    val description: String = ""
)
>>>>>>> Stashed changes
