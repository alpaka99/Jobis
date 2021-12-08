package com.ssafy.jobis.presentation.community.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.CommunityRepository
import java.lang.IllegalArgumentException

class PopularPostViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PopularPostViewModel::class.java)) {
            return PopularPostViewModel(
                communityRepository = CommunityRepository()
            ) as T
        }
        throw IllegalArgumentException("UnKnown ViewModel class")
    }
}