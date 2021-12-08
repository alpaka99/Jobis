package com.ssafy.jobis.presentation.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.study.StudyRepository


class MyFCMService: FirebaseMessagingService() {

    companion object {
        val CHANNEL_ID = "FCM"
        val CHANNEL_NAME = "MyChatFCM"

        var currentStudyId = ""
        var currentStudyTitle = ""
    }
    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mNotificationChannel: NotificationChannel
    private lateinit var chatRepo: ChatRepository  // oncreate밖에서 context를 가져오면 null 오류 발생
    private lateinit var studyRepo: StudyRepository  // oncreate밖에서 context를 가져오면 null 오류 발생

    override fun onCreate() {
        chatRepo = ChatRepository(this)
        studyRepo = StudyRepository(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            mNotificationManager = getSystemService(NotificationManager::class.java)
            mNotificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationChannel.apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500)
            }

            mNotificationManager.createNotificationChannel(mNotificationChannel)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        var studyId = ""
        var studyTitle = ""
        var isMe = false
        var userId = ""
        var nickname = ""
        var content = ""
        var fileName = ""
        var createdAt = ""

        if (remoteMessage.data.containsKey("is_entrance")) {
            remoteMessage.let {
                studyId = it.data["study_id"].toString()
                userId = it.data["user_id"].toString()
                content = it.data["content"].toString()
                createdAt = it.data["created_at"].toString()
            }
            studyRepo.addPerson(studyId)
            if (FirebaseAuth.getInstance().currentUser?.uid ?: "" != userId) {
                chatRepo.saveMessage(studyId, userId, isMe, nickname, content, fileName, createdAt, true)
            }
        } else if (remoteMessage.data.isNotEmpty()) {
            Log.d("값이 들어왔나요?", remoteMessage.data.toString())
            remoteMessage.let {
                studyId = it.data["study_id"].toString()
                studyTitle = it.data["study_title"].toString()
                userId = it.data["user_id"].toString()
                isMe = it.data["is_me"].toBoolean()
                nickname = it.data["nickname"].toString()
                content = it.data["content"].toString()
                fileName = it.data["file_name"].toString()
                createdAt = it.data["created_at"].toString()
            }

            studyRepo.updateStudyInfo(studyId, content, createdAt)
            if (studyId == currentStudyId) studyRepo.readAllChat(studyId)

            if (FirebaseAuth.getInstance().currentUser?.uid ?: "" != userId) {
                chatRepo.saveMessage(studyId, userId, isMe, nickname, content, fileName, createdAt)
            }

            if (FirebaseAuth.getInstance().currentUser?.uid ?: "" != userId && currentStudyId == "") {

                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("study_id", studyId)
                intent.putExtra("study_title", studyTitle)
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(nickname)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.ic_main)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)


                mNotificationManager.notify(studyId.hashCode(), builder.build())
            }
        }

    }
}