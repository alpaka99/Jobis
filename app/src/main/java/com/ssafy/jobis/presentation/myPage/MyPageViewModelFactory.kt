package com.ssafy.jobis.presentation.myPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.MyPageRepository
import java.lang.IllegalArgumentException

class MyPageViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPageViewModel::class.java)) {
            return MyPageViewModel(
                myPageRepository = MyPageRepository()
            ) as T
        }
        throw IllegalArgumentException("UnKnown ViewModel Class")
    }
}