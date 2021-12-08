package com.ssafy.jobis.presentation.community.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.repository.CommunityRepository
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.data.response.PostResponseList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommunitySearchViewModel(private val communityRepository: CommunityRepository) : ViewModel() {
    private val _postList = MutableLiveData<MutableList<PostResponse>>()
    val postList: LiveData<MutableList<PostResponse>> = _postList

    private val _test = MutableLiveData<String>()
    val test: LiveData<String> = _test
    fun searchPost(word: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val postList = communityRepository.loadAllPosts()
            var filterList = postList.filter { post ->
                post.title.contains(word) || post.content.contains(word)
            }
            var tmp = mutableListOf<PostResponse>()
            tmp.addAll(filterList)
            _postList.value = tmp
        }
    }

    fun test(word: String) {
        _test.value = word
    }
}