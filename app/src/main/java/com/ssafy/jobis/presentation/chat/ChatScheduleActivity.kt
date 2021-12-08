package com.ssafy.jobis.presentation.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.calendar.CalendarDatabase
import com.ssafy.jobis.data.model.calendar.RoutineScheduleDatabase
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.response.JobResponse
import com.ssafy.jobis.data.response.ScheduleResponse
import com.ssafy.jobis.databinding.ActivityChatBinding
import com.ssafy.jobis.databinding.ActivityChatCalendarBinding
import com.ssafy.jobis.presentation.chat.adapter.ChatScheduleAdapter
import com.ssafy.jobis.presentation.chat.viewmodel.ChatScheduleViewModel
import com.ssafy.jobis.presentation.chat.viewmodel.ChatScheduleViewModelFactory
import com.ssafy.jobis.presentation.community.search.CommunitySearchActivity
import com.ssafy.jobis.presentation.job.JobAdapter
import com.ssafy.jobis.presentation.job.JobViewModel
import com.ssafy.jobis.presentation.job.JobViewModelFactory
import kotlinx.android.synthetic.main.activity_chat_calendar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ChatScheduleActivity : AppCompatActivity(), ChatScheduleAdapter.OnDeleteStudyScheduleListener {
    private lateinit var binding: ActivityChatCalendarBinding
    private lateinit var chatScheduleViewModel: ChatScheduleViewModel
    private val getResultChatScheduleAddActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            chatScheduleViewModel.loadScheduleList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var currentStudyId = intent.getStringExtra("study_id").toString()

        var study_info = FirebaseDatabase.getInstance().getReference("/Study").child(currentStudyId)
        var study_title = "Study_Title"
        study_info.child("title").get()
            .addOnSuccessListener {
                study_title = it.value.toString()
                binding.textView6.text = study_title
            }.addOnFailureListener{
                println("Firebase 접근 실패")
            }


        chatScheduleViewModel = ViewModelProvider(this, ChatScheduleViewModelFactory())
            .get(ChatScheduleViewModel::class.java)

        chatScheduleViewModel.scheduleList.observe(this, Observer { scheduleList ->
            scheduleList ?:return@Observer
            updateStudyScheduleList(scheduleList, currentStudyId)
        })

        chatScheduleViewModel.loadScheduleList()

        binding.addStudySchedule.setOnClickListener {
            var intent = Intent(this, ChatScheduleAddActivity::class.java)
            intent.putExtra("study_id", currentStudyId)
            getResultChatScheduleAddActivity.launch(intent)
        }
    }

    private fun updateStudyScheduleList(scheduleList: MutableList<ScheduleResponse>, currentStudyId: String) {
        var schedules = ArrayList<Schedule>()
        for (i: Int in 0..scheduleList.size-1) {
            println("scheduleList[${i}]" + scheduleList[i].title)
            println("scheduleList[${i}]" + scheduleList[i].study_id)
            if (scheduleList[i].study_id == currentStudyId) { // studyId가 일치하는 경우만
                var year = scheduleList[i].year.toInt()
                var month = scheduleList[i].month.toInt()
                var day = scheduleList[i].day.toInt()
                var title = scheduleList[i].title
                var content = scheduleList[i].content
                var groupId = scheduleList[i].group_id.toInt()
                var studyId = scheduleList[i].study_id
                var companyName = scheduleList[i].companyName
                var startTime = scheduleList[i].start_time
                var endTime = scheduleList[i].end_time

                var schedule = Schedule(title, content, year, month, day, startTime, endTime, studyId, groupId, companyName)
                schedules.add(schedule)
            }
            var scheduleRecycler = binding.studySchedule
            println("schedules: " + schedules)
            scheduleRecycler.adapter = ChatScheduleAdapter(schedules, this)
        }
    }

    override fun onDeleteStudySchedule(schedule: Schedule) {
        var currentStudyId = intent.getStringExtra("study_id").toString()
        var study_schedules = FirebaseFirestore.getInstance().collection("study_schedules").get()
            .addOnSuccessListener {
                var studySchedules = it.documents
                for (i in 0..studySchedules.size-1) {
                    var study_id = studySchedules[i].get("study_id").toString()
                    var group_id = studySchedules[i].get("group_id").toString().toInt()
                    var title = studySchedules[i].get("title").toString()
                    var content = studySchedules[i].get("content").toString()
                    var day = studySchedules[i].get("day").toString().toInt()
                    var month = studySchedules[i].get("month").toString().toInt()
                    var year = studySchedules[i].get("year").toString().toInt()
                    var start_time = studySchedules[i].get("start_time").toString()
                    var end_time = studySchedules[i].get("end_time").toString()
                    var companyName = studySchedules[i].get("companyName").toString()
                    var currentSchedule = Schedule(title, content, year, month, day, start_time, end_time, study_id, group_id, companyName)
                    if (schedule == currentSchedule) {
                        println("현재 문서: " + studySchedules[i].id)
                        FirebaseFirestore.getInstance().collection("study_schedules").document("${studySchedules[i].id}")
                            .delete()
                            .addOnSuccessListener {
                                println("삭제 완료")
                                refreshActivity()}
                            .addOnFailureListener { println("삭제 실패")}
                    }
                }
            }.addOnFailureListener{
                println("Firebase 접근 실패")
            }
    }
    fun refreshActivity() {
        val intent = getIntent()
        finish()
        startActivity(intent)
    }

}
