package com.ssafy.jobis.presentation.study

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.data.model.study.StudyDatabase
import kotlinx.coroutines.CoroutineScope

class StudyRepository(context: Context) {

    private val db = StudyDatabase.getInstance(context)
    private val dao = db?.getStudyDao()

    fun getAllStudy(): LiveData<List<Study>>? {
        return dao?.getAllStudy()
    }

    fun updateStudyInfo(studyId: String, lastChat: String, lastDate: String) {
        if (dao == null) return
        val study = dao.getStudy(studyId)
        study.apply {
            last_chat = lastChat
            last_date = lastDate
            unread_chat_cnt ++
        }
        dao.updateStudy(study)
    }

    fun addPerson(studyId: String) {
        if (dao == null) return
        val study = dao.getStudy(studyId)
        study.current_user ++
        dao.updateStudy(study)
    }

    fun readAllChat(studyId: String) {
        if (dao == null) return
        val study = dao.getStudy(studyId)
        study.unread_chat_cnt = 0
        dao.updateStudy(study)
    }

    fun removeStudy(studyId: String) {
        if (dao == null) return

        dao.removeStudy(studyId)
    }
}