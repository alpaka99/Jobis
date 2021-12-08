package com.ssafy.jobis.presentation.chat.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.repository.JobRepository
import com.ssafy.jobis.data.repository.ScheduleRepository
import com.ssafy.jobis.data.response.JobResponse
import com.ssafy.jobis.data.response.ScheduleResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatScheduleViewModel(private val scheduleRepository: ScheduleRepository): ViewModel() {
    private val _scheduleList = MutableLiveData<MutableList<ScheduleResponse>>()
    val scheduleList: LiveData<MutableList<ScheduleResponse>> = _scheduleList

    fun loadScheduleList() {
        CoroutineScope(Dispatchers.Main).launch {
            val res = scheduleRepository.loadJobList()
            res.sortWith(compareBy({it.year}, {it.month}, {it.day}))
            _scheduleList.value = res
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