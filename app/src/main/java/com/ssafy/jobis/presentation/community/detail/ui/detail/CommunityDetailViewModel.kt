package com.ssafy.jobis.presentation.community.detail.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ssafy.jobis.data.model.Report.Report
import com.ssafy.jobis.data.model.community.Comment
import com.ssafy.jobis.data.repository.CommunityRepository
import com.ssafy.jobis.data.response.PostResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.HashMap

class CommunityDetailViewModel(private val communityRepository: CommunityRepository) : ViewModel() {
    private val _post = MutableLiveData<PostResponse>()
    val post: LiveData<PostResponse> = _post
    private val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked
    private val _likeCount = MutableLiveData<Int>()
    val likeCount: LiveData<Int> = _likeCount
    private val _comments = MutableLiveData<MutableList<Comment>>()
    val comments: LiveData<MutableList<Comment>> = _comments
    private val _deleted = MutableLiveData<Boolean>()
    val deleted: LiveData<Boolean> = _deleted
    private val _reportResult = MutableLiveData<Boolean>()
    val reportResult: LiveData<Boolean> = _reportResult


    fun loadPost(id: String, uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val postResponse = communityRepository.loadPostDetail(id)
            _post.value = postResponse!!
            val commentList = mutableListOf<Comment>()
            for (item in postResponse.comment_list) {
                commentList.add(Comment.from(item as HashMap<String, Any>))
            }
            _comments.value = commentList
            var flag = false
            for (item in postResponse.like) {
                if (item == uid) {
                    flag = true
                    break
                }
            }
            _isLiked.value = flag
            _likeCount.value = postResponse.like.size
        }
    }

    fun deletePost(id: String, uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _deleted.value = communityRepository.deletePost(id, uid)
        }
    }

    fun updateLike(post_id: String, uid: String) {
        communityRepository.updateLike(_isLiked.value!!, post_id, uid)
        if (isLiked.value == true) {
            _likeCount.value = _likeCount.value?.minus(1)
        } else if (isLiked.value == false) {
            _likeCount.value = _likeCount.value?.plus(1)
        }
        _isLiked.value = !_isLiked.value!!
    }

    fun createComment(text: String, post_id: String, uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val commentResponse = communityRepository.createComment(text, post_id, uid)
            if (commentResponse == true) {
                loadPost(post_id, uid)
            }
        }
    }

    fun deleteComment(post_id: String, comment: Comment, uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Log.d("test", "${post_id} ${comment} ${uid}")
            val response = communityRepository.deleteComment(post_id, comment, uid)
            if (response == true) {
                loadPost(post_id, uid)
            }
        }
    }

    fun getPost(): PostResponse? {
        return _post.value
    }

    fun reportPost(post_id: String, uid: String, reason: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val res = communityRepository.reportPost(Report(post_id, uid, reason))
            _reportResult.value = res
        }
    }
}