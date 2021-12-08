package com.ssafy.jobis.presentation.myPage

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.model.community.Comment
import com.ssafy.jobis.data.repository.MyPageRepository
import com.ssafy.jobis.data.response.PostResponseList
import com.ssafy.jobis.presentation.login.Jobis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyPageViewModel(private val myPageRepository: MyPageRepository): ViewModel() {

    private val _myLikeList = MutableLiveData<PostResponseList>()
    val myLikeList: LiveData<PostResponseList> = _myLikeList

    private val _myPostList = MutableLiveData<PostResponseList>()
    val myPostList: LiveData<PostResponseList> = _myPostList

    private val _myCommentList = MutableLiveData<MutableList<Comment>>()
    val myCommentList: LiveData<MutableList<Comment>> = _myCommentList

    private val _myJobList = MutableLiveData<List<Schedule>>()
    val myJobList: LiveData<List<Schedule>> = _myJobList

    private val _isScheduleDeleted = MutableLiveData<Boolean>()
    val isScheduleDeleted: LiveData<Boolean> = _isScheduleDeleted

    private val _isNicknameChanged = MutableLiveData<Boolean>()
    val isNicknameChanged: LiveData<Boolean> = _isNicknameChanged

    private val _isUserDeleted = MutableLiveData<Boolean>()
    val isUserDeleted: LiveData<Boolean> = _isUserDeleted

    fun loadMyLikeList(uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val res = myPageRepository.loadMyLikePosts(uid)
            _myLikeList.value = res
        }
    }

    fun loadMyPostList(uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val res = myPageRepository.loadMyPosts(uid)
            _myPostList.value = res
        }
    }

    fun loadMyCommentList(uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val res = myPageRepository.loadMyCommentPosts(uid)
            _myCommentList.value = res
        }
    }

    fun loadMyJobList(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = myPageRepository.loadMyJobList(context)
            if (res is List<Schedule>) {
                var a = res.sortedWith(compareBy({it.year}, {it.month}, {it.day}))
                for (item in a) {
                    Log.d("test", "dd ${getDDay(item.year, item.month+1, item.day)}")
                }
                var b = a.filter {
                    getDDay(it.year, it.month+1, it.day) in 0..30
                }.toMutableList()
                _myJobList.postValue(b)
            }
        }
    }

    fun updateNickName(uid: String, nickName: String) {
        CoroutineScope(Dispatchers.Main).launch {
            var res = myPageRepository.updateNickName(uid, nickName)
            if (res) {
                Jobis.prefs.setString("nickname", nickName)
            }
            _isNicknameChanged.value = res
        }
    }

    fun deleteSchedule(schedule: Schedule, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = myPageRepository.deleteSchedule(schedule, context)
            _isScheduleDeleted.postValue(res)
        }
    }

    fun deleteAccount(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            //순서대로 실행
            var res = false
            val job1 = myPageRepository.deleteMyDatabase(uid)
            if (job1) {
                res = myPageRepository.deleteAuthentication()
            }
            _isUserDeleted.postValue(res)
        }
    }

    private fun getDDay(year: Int, month: Int, day: Int): Long {

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val now_time = sdf.format(Date()).split("-")
        val nowDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, now_time[0].toInt())
            set(Calendar.MONTH, now_time[1].toInt())
            set(Calendar.DAY_OF_MONTH, now_time[2].toInt())
        }.timeInMillis

        val dueDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }.timeInMillis

        val dDay = getIgnoredTimeDays(dueDay) - getIgnoredTimeDays(nowDay)
        return dDay / (24*60*60*1000)
    }

    private fun getIgnoredTimeDays(time: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

}