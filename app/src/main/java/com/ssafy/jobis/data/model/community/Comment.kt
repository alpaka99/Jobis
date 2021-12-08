package com.ssafy.jobis.data.model.community

import com.google.firebase.Timestamp
import java.util.*

data class Comment(
    val user_nickname: String,
    val user_id: String,
    val post_id: String,
    val created_at: Timestamp = Timestamp(Date()),
    val content: String
) {
    companion object {
        fun from(map: HashMap<String, Any>): Comment {
            return object {
                val user_nickname by map
                val user_id by map
                val post_id by map
                val created_at by map
                val content by map
                val data = Comment(
                    user_nickname as String,
                    user_id as String,
                    post_id as String,
                    created_at as Timestamp,
                    content as String
                )
            }.data
        }
    }
}
