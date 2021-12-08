package com.ssafy.jobis.presentation.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.repository.CommunityRepository
import com.ssafy.jobis.data.response.PostResponseList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommunityViewModel(private val communityRepository: CommunityRepository) : ViewModel() {
    private val _postList = MutableLiveData<PostResponseList>()
    val postList: LiveData<PostResponseList> = _postList
    private val _recentPostList = MutableLiveData<PostResponseList>()
    val recentPostList: LiveData<PostResponseList> = _recentPostList
    private val _popularPostList = MutableLiveData<PostResponseList>()
    val popularPostList: LiveData<PostResponseList> = _popularPostList

    fun loadAllPosts() {
        CoroutineScope(Dispatchers.Main).launch {
            _postList.value = communityRepository.loadAllPosts()
        }
    }

    fun loadRecentPosts() {
        CoroutineScope(Dispatchers.Main).launch {
            _recentPostList.value = communityRepository.loadRecentPosts()
        }
    }

    fun loadPopularPosts() {
        CoroutineScope(Dispatchers.Main).launch {
            _popularPostList.value = communityRepository.loadPopularPosts()
        }
    }
}