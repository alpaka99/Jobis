package com.ssafy.jobis.presentation.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.data.repository.AdminRepository
import com.ssafy.jobis.data.response.PostResponseList
import com.ssafy.jobis.data.response.ReportResponse
import com.ssafy.jobis.data.response.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel(private val adminRepository: AdminRepository): ViewModel() {
    private val _userList = MutableLiveData<MutableList<UserResponse>>()
    val userList: LiveData<MutableList<UserResponse>> = _userList

    private val _reportList = MutableLiveData<MutableList<ReportResponse>>()
    val reportList: LiveData<MutableList<ReportResponse>> = _reportList

    private val _postList = MutableLiveData<PostResponseList>()
    val postList: LiveData<PostResponseList> = _postList

    private val _isUserDeleted = MutableLiveData<Boolean>()
    val isUserDeleted: LiveData<Boolean> = _isUserDeleted

    fun loadAllUser() {
        CoroutineScope(Dispatchers.Main).launch {
            _userList.value = adminRepository.loadAllUsers()
        }
    }

    fun loadAllReport() {
        CoroutineScope(Dispatchers.Main).launch {
            val reportList = adminRepository.loadAllReports()
            _postList.value = adminRepository.loadReportedPosts(reportList)

        }
    }

    fun deleteUser(user: UserResponse) {
        var res = false
        val auth = Firebase.auth
        CoroutineScope(Dispatchers.Main).launch {
            auth.signInWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user!!.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    res = true
                                    Log.d("test", "1")
                                }
                            }
                    }
                }.await()
            Log.d("test", "2")
            _isUserDeleted.value = res
        }
    }

}