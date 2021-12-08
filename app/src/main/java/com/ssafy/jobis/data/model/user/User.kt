package com.ssafy.jobis.data.model.user

data class User(
    val article_list: MutableList<String> = mutableListOf(),
    val email: String,
    val password: String,
    val nickname: String? = null,
    val comment_list: MutableList<Any> = mutableListOf(),
    val study_list: MutableList<String> = mutableListOf(),
    val schedule_list: MutableList<String> = mutableListOf(),
    val like_post_list: MutableList<String> = mutableListOf()
)
