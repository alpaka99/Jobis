package com.ssafy.jobis.presentation.community.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.data.repository.CommunityRepository
import java.lang.IllegalArgumentException

class CommunitySearchViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunitySearchViewModel::class.java)) {
            return CommunitySearchViewModel(
                communityRepository = CommunityRepository()
            ) as T
        }
        throw IllegalArgumentException("UnKnown ViewModel Class")
    }
}