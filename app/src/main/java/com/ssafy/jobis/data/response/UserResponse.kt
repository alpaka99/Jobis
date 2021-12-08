package com.ssafy.jobis.data.response

data class UserResponse(
    val article_list: MutableList<String>,
    val email: String,
    val password: String,
    val nickname: String,
    val comment_list: MutableList<Any>,
    val study_list: MutableList<String>,
    val schedule_list: MutableList<String>,
    val like_post_list: MutableList<String>,
    val id: String
) {
    companion object {
        fun from(map: Map<String, Any>, user_id: String): UserResponse {
            return object {
                val article_list by map
                val email by map
                val password by map
                val nickname by map
                val comment_list by map
                val study_list by map
                val schedule_list by map
                val like_post_list by map
                val data = UserResponse(
                    article_list as MutableList<String>,
                    email as String,
                    password as String,
                    nickname as String,
                    comment_list as MutableList<Any>,
                    study_list as MutableList<String>,
                    schedule_list as MutableList<String>,
                    like_post_list as MutableList<String>,
                    id=user_id
                )
            }.data
        }
    }
}