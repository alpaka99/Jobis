package com.ssafy.jobis.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.CommunityRepository
import java.lang.IllegalArgumentException

class CommunityViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            return CommunityViewModel(
                communityRepository = CommunityRepository()
            ) as T
        }
        throw IllegalArgumentException("UnKnown ViewModel class")
    }
}