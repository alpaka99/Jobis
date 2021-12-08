package com.ssafy.jobis.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.ScheduleRepository
import java.lang.IllegalArgumentException

class ChatScheduleViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatScheduleViewModel::class.java)) {
            return ChatScheduleViewModel(
                scheduleRepository = ScheduleRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}