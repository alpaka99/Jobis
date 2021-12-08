package com.ssafy.jobis.presentation.job

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.JobRepository
import java.lang.IllegalArgumentException

class JobViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
            return JobViewModel(
                jobRepository = JobRepository()
            ) as T
        }
        throw IllegalArgumentException("UnKown ViewModel Class")
    }
}