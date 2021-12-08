package com.ssafy.jobis.presentation.community.recent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.repository.CommunityRepository
import com.ssafy.jobis.data.response.PostResponseList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentPostViewModel(private val communityRepository: CommunityRepository) : ViewModel() {
    private val _recentPostList = MutableLiveData<PostResponseList>()
//    val recentPostList: LiveData<PostResponseList> = _recentPostList
    private val _filteredPostList = MutableLiveData<PostResponseList>()
    val filteredPostList: LiveData<PostResponseList> = _filteredPostList
    fun loadRecentPosts() {
        CoroutineScope(Dispatchers.Main).launch {
            val res = communityRepository.loadRecentPosts()
            _recentPostList.value = res
            _filteredPostList.value = res
        }
    }

    fun filterPost(pos: Int) {
        if (_recentPostList.value == null) {
            return
        }
        if (pos == 0) {
            _filteredPostList.value = _recentPostList.value
            return
        }
        val category = listOf("전체", "경영/사무", "마케팅/광고", "유통/물류", "영업", "IT",
            "생산/제조", "연구개발/설계", "전문직", "디자인/미디어", "건설", "서비스", "기타").get(pos)
        val listData = PostResponseList()
        for (item in _recentPostList.value!!) {
            if (item.category == category) {
                listData.add(item)
            }
        }
        Log.d("test", "dsf ${listData}")
        _filteredPostList.postValue(listData)
    }
}