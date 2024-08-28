package com.example.life_assistant.ViewModel

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.life_assistant.Event
import com.example.life_assistant.Repository.MemberRepository
import com.example.life_assistant.Screen.convertLongToDate
import com.example.life_assistant.data.Colors
import com.example.life_assistant.data.Member
import com.example.life_assistant.data.MemberEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MemberViewModel @Inject constructor(
    val auth: FirebaseAuth,
    private val memberRepository: MemberRepository
): ViewModel(){
    val database = FirebaseDatabase.getInstance("https://life-assistant-27ae8-default-rtdb.europe-west1.firebasedatabase.app/")
    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val registrationSuccess = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val member = mutableStateOf<Member?>(null)
    val colors = mutableStateOf<Colors?>(null)
    val email: MutableLiveData<String> = MutableLiveData()
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
                                        val memberEntity = MemberEntity(
                                            uid = userId,
                                            name = name,
                                            birthday = formattedBirthday
                                        )
                                        //insertMember(memberEntity)

                                        registrationSuccess.value = true
                                        Log.d("Registration", "viewmodel_registrationSuccess: ${registrationSuccess.value}")
                                    } else {
                                        handleException(moveTask.exception, "未能成功存入使用者資料")
                                    }
                                }
                            } else {
                                handleException(databaseTask.exception, "未能成功存入註冊資料")
                            }
                        }
                    }
                } else {
                    handleException(authTask.exception, "註冊失敗")
                }
                inProgress.value = false
            }
    }

    //插入會員資料到本地端資料庫
    fun insertMember(memberEntity: MemberEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            memberRepository.insert(memberEntity)
        }
    }

    // 儲存習慣時間
    fun saveHabitTimes(wakeHour: Int, wakeMinute: Int, sleepHour: Int, sleepMinute: Int,readingTag:String,sportTag:String,workTag:String,leisureTag:String,houseworkTag:String) {
        val memberId = auth.currentUser?.uid ?: return

        val habitRef = database.getReference("members").child(memberId)
        val wakeTime = String.format("%02d:%02d", wakeHour, wakeMinute)
        val sleepTime = String.format("%02d:%02d", sleepHour, sleepMinute)

        val habits = mapOf(
            "wakeTime" to wakeTime,
            "sleepTime" to sleepTime,
            "readingTag" to readingTag,
            "sportTag" to sportTag,
            "workTag" to workTag,
            "leisureTag" to leisureTag,
            "houseworkTag" to houseworkTag,
        )

        habitRef.updateChildren(habits).addOnSuccessListener {
            Log.d("Firebase", "Habit times saved successfully")

//            viewModelScope.launch(Dispatchers.IO) {
//                val existingMember = memberRepository.getMemberByUid(memberId)
//                if (existingMember != null) {
//                    val updatedMember = existingMember.copy(
//                        wake_time = wakeTime,
//                        sleep_time = sleepTime
//                    )
//                    memberRepository.update(updatedMember)
//                }
//            }

            auth.signOut()
            signedIn.value = false
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
                    val memberId = auth.currentUser?.uid ?: ""
                    //checkAndInsertMember(memberId)
                    signedIn.value = true
                    Log.d("AlertDialog", "sign: $signedIn.value ")
                } else {
                    handleException(authTask.exception, "登入失敗")
                }
                inProgress.value = false
            }
    }

    //如果firebase註冊過，但非這台手機的話
    private fun checkAndInsertMember(memberId: String) {
        database.getReference("members").child(memberId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val birthday = snapshot.child("birthday").getValue(String::class.java) ?: ""
                    val wakeTime = snapshot.child("habits").child("wakeTime").getValue(String::class.java) ?: ""
                    Log.d("test","$wakeTime")
                    val sleepTime = snapshot.child("habits").child("sleepTime").getValue(String::class.java) ?: ""
                    Log.d("test","$sleepTime")


                    viewModelScope.launch(Dispatchers.IO) {
                        val existingMember = memberRepository.getUid(memberId)
                        if (existingMember == null) {
                            val memberEntity = MemberEntity(
                                uid = memberId,
                                name = name,
                                birthday = birthday,
                                wake_time = wakeTime,
                                sleep_time = sleepTime
                            )
                            insertMember(memberEntity)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, "取得使用者資料失敗")
            }
    }

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
            Log.d("AuthException", "Error Code: ${exception.errorCode}")
            return when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "電子郵件格式錯誤"
                "ERROR_WEAK_PASSWORD" -> "密碼至少大於等於六位"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "已經有相同的電子郵件被註冊"
                "ERROR_INVALID_CREDENTIAL" -> "帳號或密碼不正確"
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
        }
    }

    //修改資料
    fun updateMemberData(name: String, birthday: String, sleepTime: String, wakeTime: String) {
        val memberId = auth.currentUser?.uid ?: return

        val memberRef = database.getReference("members").child(memberId)

        memberRef.child("name").setValue(name).addOnSuccessListener {
            memberRef.child("birthday").setValue(birthday).addOnSuccessListener {
                memberRef.child("sleepTime").setValue(sleepTime).addOnSuccessListener {
                    memberRef.child("wakeTime").setValue(wakeTime).addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) { // 確保在IO執行緒上運行
                            try {
                                // 從 Room Database 獲取當前成員資料
                                val currentMember = memberRepository.getMemberByUid(memberId)
                                if (currentMember != null) {
                                    // 更新成員資料
//                                    val updatedMember = currentMember.copy(name = name, birthday = birthday, sleepTime = sleepTime, wakeTime = wakeTime)
//                                    memberRepository.update(updatedMember)
//                                    // 在主執行緒上顯示對話框和更新UI
//                                    withContext(Dispatchers.Main) {
//                                        showDialog.value = true
//                                        getData() // 獲取更新後的資料
//                                    }
                                } else {
                                    Log.d("AlertDialog", "沒有找到該成員資料")
                                }
                            } catch (exception: Exception) {
                                withContext(Dispatchers.Main) {
                                    handleException(exception, "無法更新資料")
                                }
                            }
                        }
                    }.addOnFailureListener { exception ->
                        handleException(exception, "無法更新 wakeTime")
                    }
                }.addOnFailureListener { exception ->
                    handleException(exception, "無法更新 sleepTime")
                }
            }.addOnFailureListener { exception ->
                handleException(exception, "無法更新 birthday")
            }
        }.addOnFailureListener { exception ->
            handleException(exception, "無法更新 name")
        }
    }

    fun updateTags(readingTag: String,sportTag: String,workTag: String,leisureTag: String,houseworkTag: String){
        val memberId = auth.currentUser?.uid ?: return

        val habitRef = database.getReference("members").child(memberId)

        val habits = mapOf(
            "readingTag" to readingTag,
            "sportTag" to sportTag,
            "workTag" to workTag,
            "leisureTag" to leisureTag,
            "houseworkTag" to houseworkTag,
        )

        habitRef.updateChildren(habits).addOnSuccessListener {
            Log.d("Firebase", "Habit times saved successfully")

//            viewModelScope.launch(Dispatchers.IO) {
//                val existingMember = memberRepository.getMemberByUid(memberId)
//                if (existingMember != null) {
//                    val updatedMember = existingMember.copy(
//                        wake_time = wakeTime,
//                        sleep_time = sleepTime
//                    )
//                    memberRepository.update(updatedMember)
//                }
//            }
        }.addOnFailureListener { exception ->
            handleException(exception, "無法儲存使用習慣")
        }
    }

    fun updateColors(readingColor: Long,sportColor: Long,workColor: Long,leisureColor: Long,houseworkColor: Long,travelColor:Long,eatingColor:Long) {
        val memberId = auth.currentUser?.uid ?: return

        val habitRef = database.getReference("members").child(memberId).child("colors")

        val habits = mapOf(
            "readingColors" to readingColor,
            "sportColors" to sportColor,
            "workColors" to workColor,
            "leisureColors" to leisureColor,
            "houseworkColors" to houseworkColor,
            "travelColors" to travelColor,
            "eatingColors" to eatingColor
        )

        habitRef.updateChildren(habits).addOnSuccessListener {
            Log.d("Firebase", "Habit times saved successfully")

//            viewModelScope.launch(Dispatchers.IO) {
//                val existingMember = memberRepository.getMemberByUid(memberId)
//                if (existingMember != null) {
//                    val updatedMember = existingMember.copy(
//                        wake_time = wakeTime,
//                        sleep_time = sleepTime
//                    )
//                    memberRepository.update(updatedMember)A
//                }
//            }
        }.addOnFailureListener { exception ->
            handleException(exception, "無法儲存使用習慣")
        }
    }

    fun getColors() {
        val memberId = auth.currentUser?.uid ?: return
        val colorRef = database.getReference("members").child(memberId).child("colors")

        colorRef.get().addOnSuccessListener { snapshot ->
            val colorData = snapshot.getValue(Colors::class.java)
            colors.value = colorData
        }.addOnFailureListener { exception ->
            handleException(exception, "無法取得標籤顏色")
        }
    }

    private val _tagPreferences = mutableStateOf<Map<String, String>>(emptyMap())
    val tagPreferences: State<Map<String, String>> = _tagPreferences

    fun getTag() {
        val memberId = auth.currentUser?.uid ?: return
        val tagRef = database.getReference("members").child(memberId)

        tagRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val readingTag = snapshot.child("readingTag").getValue(String::class.java) ?: ""
                val sportsTag = snapshot.child("sportsTag").getValue(String::class.java) ?: ""
                val workTag = snapshot.child("workTag").getValue(String::class.java) ?: ""
                val leisureTag = snapshot.child("leisureTag").getValue(String::class.java) ?: ""
                val houseworkTag = snapshot.child("houseworkTag").getValue(String::class.java) ?: ""

                _tagPreferences.value = mapOf(
                    "讀書" to readingTag,
                    "運動" to sportsTag,
                    "工作" to workTag,
                    "娛樂" to leisureTag,
                    "生活雜務" to houseworkTag
                )
                println("back:$_tagPreferences")
            }
        }.addOnFailureListener { exception ->
            // 處理例外情況
            handleException(exception, "無法從 Firebase 抓取標籤資料")
        }
    }

    data class SimpleEvent(
        val name: String,
        val startTime: String,
        val endTime: String
    )

    //取得今天的前兩天跟後兩天
    private fun getSevenDayRange(): Pair<String, String> {
        val today = LocalDate.now()
        val startDate = today.minusDays(2)  // 前兩天
        val endDate = today.plusDays(4)    // 後四天

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return Pair(startDate.format(formatter), endDate.format(formatter))
    }

    //從標籤取得事件
    fun getEventByTag(tag: String, onEventsRetrieved: (List<SimpleEvent>) -> Unit) {
        val memberId = auth.currentUser?.uid ?: return
        val eventRef = database.getReference("members").child(memberId).child("events")

        val (startDate, endDate) = getSevenDayRange()

        eventRef.orderByChild("tags").equalTo(tag).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<SimpleEvent>()
                snapshot.children.forEach { dataSnapshot ->
                    val name = dataSnapshot.child("name").getValue(String::class.java) ?: ""
                    val startTime = dataSnapshot.child("startTime").getValue(String::class.java) ?: ""
                    val endTime = dataSnapshot.child("endTime").getValue(String::class.java) ?: ""

                    // 確保事件在範圍內
                    if (isEventWithinRange(startTime, endTime, startDate, endDate)) {
                        events.add(SimpleEvent(name, startTime, endTime))
                    }
                }
                // 回傳取得的事件列表
                onEventsRetrieved(events)
            }

            override fun onCancelled(error: DatabaseError) {
                // 處理錯誤
                Log.e("Firebase", "Failed to fetch events by tag", error.toException())
            }
        })
    }

    //檢查範圍
    fun isEventWithinRange(startTime: String, endTime: String, startDate: String, endDate: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val eventStart = LocalDate.parse(startTime.substring(0, 10), formatter)
        val eventEnd = LocalDate.parse(endTime.substring(0, 10), formatter)
        val rangeStart = LocalDate.parse(startDate, formatter)
        val rangeEnd = LocalDate.parse(endDate, formatter)

        return (eventStart.isBefore(rangeEnd) || eventStart.isEqual(rangeEnd)) &&
                (eventEnd.isAfter(rangeStart) || eventEnd.isEqual(rangeStart))
    }



}