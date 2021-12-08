package com.ssafy.jobis.data.response

import com.google.firebase.Timestamp
import com.ssafy.jobis.data.model.community.Comment

data class PostResponse(

    val title: String,
    val content: String,
    val created_at: Timestamp,
    val updated_at: Timestamp,
    val comment_list: MutableList<Any>,
    val user_id: String,
    val user_nickname: String,
    val like: MutableList<Any>,
    val category: String,
    val id: String? = null
) {
    companion object {
        fun from(map: Map<String, Any>, post_id: String? = null): PostResponse {
            return object {
                val title by map
                val content by map
                val created_at by map
                val updated_at by map
                val comment_list by map
                val user_id by map
                val user_nickname by map
                val like by map
                val category by map
                val data = PostResponse(
                    title as String,
                    content as String,
                    created_at as Timestamp,
                    updated_at as Timestamp,
                    comment_list as MutableList<Any>,
                    user_id as String,
                    user_nickname as String,
                    like as MutableList<Any>,
                    category as String,
                    id=post_id
                )
            }.data
        }
    }
}
