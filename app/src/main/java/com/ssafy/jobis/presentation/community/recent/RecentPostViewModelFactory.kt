package com.ssafy.jobis.presentation.community.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.CommunityRepository
import java.lang.IllegalArgumentException

class RecentPostViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecentPostViewModel::class.java)) {
            return RecentPostViewModel(
                communityRepository = CommunityRepository()
            ) as T
        }
        throw IllegalArgumentException("UnKnown ViewModel class")
    }
}