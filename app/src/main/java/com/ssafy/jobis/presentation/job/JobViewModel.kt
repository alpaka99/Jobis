package com.ssafy.jobis.presentation.job

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.model.calendar.CalendarDatabase
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.repository.JobRepository
import com.ssafy.jobis.data.response.JobResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JobViewModel(private val jobRepository: JobRepository): ViewModel() {

    private val _jobList = MutableLiveData<MutableList<JobResponse>>()
    val jobList: LiveData<MutableList<JobResponse>> = _jobList
    private val _myJobList = MutableLiveData<List<Schedule>>()
    val myJobList: LiveData<List<Schedule>> = _myJobList
    private val _scheduleResult = MutableLiveData<Schedule?>()
    val scheduleResult: LiveData<Schedule?> = _scheduleResult
    private val _isScheduleDeleted = MutableLiveData<Boolean>()
    val isScheduleDeleted: LiveData<Boolean> = _isScheduleDeleted
    fun loadJobList() {
        CoroutineScope(Dispatchers.Main).launch {
            val res = jobRepository.loadJobList()
            res.sortWith(compareBy({it.year}, {it.month}, {it.day}))
            val a = res.filter {
                checkDeadLine(it.year.toInt(), it.month.toInt(), it.day.toInt())
            }.toMutableList()
            _jobList.value = a
        }
    }

    fun insertSchedule(schedule: Schedule, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val schedule = jobRepository.insertSchedule(schedule, context)
            _scheduleResult.postValue(schedule)
        }
    }

    fun loadMyJobList(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = jobRepository.loadMyJobList(context)
            if (res is List<Schedule>) {
                val a = res.sortedWith(compareBy({it.year}, {it.month}, {it.day}))
//                val a = res.filter {
//                    checkDeadLine(it.year.toInt(), it.month.toInt(), it.day.toInt())
//                }.toMutableList()
                _myJobList.postValue(a)
            }
        }
    }

    fun deleteSchedule(schedule: Schedule, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = jobRepository.deleteSchedule(schedule, context)
            _isScheduleDeleted.postValue(res)
        }
    }

    fun checkDeadLine(year: Int, month: Int, day: Int): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val now_time = sdf.format(Date()).split("-")
        val now_year = now_time[0].toInt()
        val now_month = now_time[1].toInt()
        val now_day = now_time[2].toInt()
        if (now_year > year) {
            return false
        } else if (now_year < year) {
            return true
        } else {
            if (now_month > month) {
                return false
            } else if (now_month < month) {
                return true
            } else {
                return now_day <= day
            }
        }
    }
}