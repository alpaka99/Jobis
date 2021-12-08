package com.ssafy.jobis.presentation.chat.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ssafy.jobis.data.model.study.StudyWithChats
import com.ssafy.jobis.presentation.chat.ChatActivity
import com.ssafy.jobis.presentation.chat.ChatRepository
import com.ssafy.jobis.presentation.chat.MyFCMService.Companion.currentStudyId
import com.ssafy.jobis.presentation.chat.MyFCMService.Companion.currentStudyTitle
import com.ssafy.jobis.presentation.study.StudyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ChatViewModel(application: Application): AndroidViewModel(application) {


    private val _studyWithChats: LiveData<StudyWithChats>
    val studyWithChats: LiveData<StudyWithChats> get() = _studyWithChats
    var uid: String = ""
    var nickname: String = ""
    var fileReference: String = ""
    var chooseFileName = ""
    private var isFirstTime = false

    private val chatRepo = ChatRepository(application)
    private val studyRepo = StudyRepository(application)

    init {
        Log.d("현재 스터디", currentStudyId)
        _studyWithChats = chatRepo.getChatList(currentStudyId)!!
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener {
            nickname = it["nickname"].toString()
            if( isFirstTime) {
                entrance()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun sendMessage(content: String, fileName: String = "") {

        val date = Date()
        val createdAt = SimpleDateFormat("yyyy-MM-dd a hh:mm").format(date)

        saveMessage(content, fileName, createdAt)
        uploadMessage(content, fileName, createdAt)
    }

    fun saveMessage(content: String, fileName: String, createdAt: String) {
        CoroutineScope(Dispatchers.IO).launch {
            chatRepo.saveMessage(currentStudyId, uid, true, nickname, content, fileName, createdAt)
        }
    }

    fun uploadMessage(content: String, fileName: String, createdAt: String) {
        if (uid=="") return
        CoroutineScope(Dispatchers.IO).launch {
            chatRepo.uploadMessage(currentStudyId, currentStudyTitle, uid, false, nickname, content, fileName, createdAt)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun entrance() {
        if (uid=="") return
        Log.d("닉네임", nickname)
        val date = Date()
        val createdAt = SimpleDateFormat("yyyy-MM-dd a hh:mm").format(date)

        CoroutineScope(Dispatchers.IO).launch {
            chatRepo.entrance(currentStudyId, uid, nickname, createdAt)
        }
    }

    fun readAllChat() {
        CoroutineScope(Dispatchers.IO).launch {
            studyRepo.readAllChat(currentStudyId)
        }
    }

    fun setFirstTime() {
        isFirstTime = true
    }

    fun exitStudy(studyId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            chatRepo.removeChat(studyId)
            studyRepo.removeStudy(studyId)
        }
    }
}