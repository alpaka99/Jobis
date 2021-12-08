package com.ssafy.jobis.presentation.study

import android.app.Application
import androidx.lifecycle.*
import androidx.room.ColumnInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.presentation.study.adapter.SearchResultAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat


class StudyViewModel(application: Application): AndroidViewModel(application) {

    private val _studyList: LiveData<List<Study>>
    val studyList: LiveData<List<Study>> get() = _studyList
    val repo = StudyRepository(application)

    init {
        _studyList = repo.getAllStudy()!!
    }

}