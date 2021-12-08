package com.ssafy.jobis.data.repository

import android.content.ContentValues.TAG
import android.text.BoringLayout
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.data.response.PostResponseList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssafy.jobis.data.model.Report.Report
import com.ssafy.jobis.data.model.community.Comment
import com.ssafy.jobis.presentation.login.Jobis
import kotlinx.coroutines.tasks.await
import java.security.Timestamp

class CommunityRepository {
    var db = FirebaseFirestore.getInstance()
    suspend fun loadAllPosts(): PostResponseList {
        var postList = PostResponseList()
        return try {
//            var db = FirebaseFirestore.getInstance()
            db.collection("posts")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        postList.add(PostResponse.from(document.data, document.id))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }.await()
            postList
        } catch(e: Throwable) {
            e.printStackTrace()
            postList
        }
    }

    suspend fun loadRecentPosts(): PostResponseList {
        var postList = PostResponseList()
        return try {
//            val db = FirebaseFirestore.getInstance()
            db.collection("posts")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        document.data
                        postList.add(PostResponse.from(document.data, document.id))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }.await()
            postList
        } catch(e: Throwable) {
            e.printStackTrace()
            postList
        }
    }

    suspend fun loadPopularPosts(): PostResponseList {
        var postList = PostResponseList()
        return try {
//            val db = FirebaseFirestore.getInstance()
            db.collection("posts")
                .orderBy("like", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        postList.add(PostResponse.from(document.data, document.id))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }.await()
            postList
        } catch(e: Throwable) {
            e.printStackTrace()
            postList
        }
    }

    suspend fun loadPostDetail(id: String): PostResponse? {
        var post: PostResponse? = null
        return try {
//            val db = FirebaseFirestore.getInstance()
            db.collection("posts").document(id)
                .get()
                .addOnSuccessListener { result ->
                    post = result.data?.let { PostResponse.from(it) }
                }
                .await()
            post
        } catch(e: Throwable) {
            e.printStackTrace()
            post
        }
    }

    suspend fun deletePost(post_id: String, uid: String): Boolean {
//        val db = FirebaseFirestore.getInstance()
        var res = false
        val postRef = db.collection("posts").document(post_id)
        val userRef = db.collection("users").document(uid)
        return try {
            db.runBatch { batch ->
                batch.delete(postRef)
                batch.update(userRef, "article_list", FieldValue.arrayRemove(post_id))
            }
                .addOnSuccessListener {
                    res = true
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "${e}")
                }
                .await()
            res
        } catch(e: Throwable) {
            e.printStackTrace()
            res
        }
    }

    fun updateLike(isLiked: Boolean, post_id: String, uid: String) {
//        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("posts").document(post_id)
        val userRef = db.collection("users").document(uid)
        db.runBatch { batch ->
            if (isLiked) {
                batch.update(docRef,"like", FieldValue.arrayRemove(uid))
                batch.update(userRef,"like_post_list", FieldValue.arrayRemove(post_id))
            } else {
                batch.update(docRef,"like", FieldValue.arrayUnion(uid))
                batch.update(userRef,"like_post_list", FieldValue.arrayUnion(post_id))
            }
        }
    }

    suspend fun createComment(text: String, post_id: String, uid: String): Boolean? {
        var commentResponse = false
//        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("posts").document(post_id)
        val userRef = db.collection("users").document(uid)
        val comment = Comment(
            user_nickname= Jobis.prefs.getString("nickname", "???"),
            user_id=uid,
            post_id=post_id,
            content=text
        )
        return try {
            db.runBatch { batch ->
                batch.update(docRef, "comment_list", FieldValue.arrayUnion(comment))
                batch.update(userRef, "comment_list", FieldValue.arrayUnion(comment))
            }
                .addOnSuccessListener {
                    commentResponse = true
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }
                .await()
            commentResponse
        } catch(e: Throwable) {
            e.printStackTrace()
            commentResponse
        }
    }

    suspend fun deleteComment(post_id: String, comment: Comment, uid: String): Boolean {
//        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("posts").document(post_id)
        val userRef = db.collection("users").document(uid)
        var response = false
        return try {
            db.runBatch { batch ->
                batch.update(docRef, "comment_list", FieldValue.arrayRemove(comment))
                batch.update(userRef, "comment_list", FieldValue.arrayRemove(comment))
            }
                .addOnSuccessListener {
                    response = true
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }
                .await()
            response
        } catch(e: Throwable) {
            e.printStackTrace()
            response
        }
    }

    suspend fun reportPost(report: Report): Boolean {
        var ret = false
        return try {
            db.collection("reports")
                .add(report)
                .addOnSuccessListener {
                    ret = true
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }
                .await()
            ret
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

}