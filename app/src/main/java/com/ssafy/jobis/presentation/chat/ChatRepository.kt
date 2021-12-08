package com.ssafy.jobis.presentation.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.ssafy.jobis.data.model.study.Chat
import com.ssafy.jobis.data.model.study.StudyDatabase
import com.ssafy.jobis.data.model.study.StudyWithChats
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ChatRepository(context: Context) {

    private val db = StudyDatabase.getInstance(context)
    private val dao = db?.getStudyDao()

    fun getChatList(studyId: String): LiveData<StudyWithChats>? {
        return dao?.getChatList(studyId)
    }

    fun saveMessage(studyId: String, userId: String, isMe: Boolean, nickname: String, content: String, fileName: String, createdAt: String, isEntrance: Boolean = false) {
        val chat = Chat(
            study_id = studyId,
            user_id = userId,
            is_me = isMe,
            nickname = nickname,
            content = content,
            file_name = fileName,
            is_entrance = isEntrance,
            created_at = createdAt)
        dao?.insertChat(chat)
    }

    fun uploadMessage(studyId: String, studyTitle: String, userId: String, isMe: Boolean, nickname: String, content: String, fileName: String, createdAt: String) {

        val root = JSONObject()
        val data = JSONObject()
        data.apply {
            put("study_id", studyId)
            put("study_title", studyTitle)
            put("user_id", userId)
            put("is_me", isMe)
            put("nickname", nickname)
            put("content", content)
            put("file_name", fileName)
            put("created_at", createdAt)
        }
        root.put("data", data)
//        val notification = JSONObject()
//        notification.put("title", "반갑습니다")
//        notification.put("body", "안녕하세요ㅋㅋㅋ")
//        notification을 넣으면 자동으로 알림이 옴. 커스텀하고싶다면 서비스에서 재정의해야 됨
//        root.put("notification", notification)
        root.put("to", "/topics/${studyId}")

        sendFCM(root)
    }

    fun entrance(studyId: String, uid: String, nickname: String, createdAt: String) {
        val root = JSONObject()
        val data = JSONObject()
        data.apply {
            put("is_entrance", true)
            put("study_id", studyId)
            put("user_id", uid)
            put("content", "${nickname}님이 입장하셨습니다.")
            put("created_at", createdAt)
        }
        root.put("data", data)
        root.put("to", "/topics/${studyId}")

        sendFCM(root)
    }

    fun sendFCM(root: JSONObject) {
        val url = URL("https://fcm.googleapis.com/fcm/send")
        val httpConnect = url.openConnection() as HttpURLConnection
        httpConnect.apply {
            requestMethod = "POST"
            doOutput = true
            doInput = true
            // 서버 키는 받아서 사용
            addRequestProperty("Authorization", "key=AAAAyi-siU4:APA91bH-r-bKju0IKHUADgXlDlpIZLEo1blM7TaYHW_k_DWQZzwwRgxlLvqLdkMoTnSAOD1NXiDWf2tBb15eY3K4AqFXeY7HYk4n21rn1UqEweNOU6TXjMh9E7vQRJCDM1dj17CmAcIv")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-type", "application/json")
        }
        val os = httpConnect.outputStream
        os.write(root.toString().toByteArray(charset("utf-8")))
        os.flush()
        val responseCode = httpConnect.responseCode
        if (responseCode == 200) {
            Log.d("성공", "성공")
//            Log.d("성공", inputStreamToString(httpConnect.inputStream))
        } else {
            Log.d("실패", "실패")
        }
    }

    private fun inputStreamToString(inputStream: InputStream): String {
        val stringBuilder = StringBuilder()
        val scanner = Scanner(inputStream)
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine())
        }
        return stringBuilder.toString()
    }

    fun removeChat(studyId: String) {
        if (dao == null) return
        dao.removeChat(studyId)
    }
}