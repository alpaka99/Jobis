package com.ssafy.jobis.data.model.study

import androidx.lifecycle.LiveData
import androidx.room.Embedded
import androidx.room.Relation

data class StudyWithChats (
    @Embedded val study: Study,
    @Relation(
        parentColumn = "study_id",
        entityColumn = "study_id"
    )
    val chats : List<Chat>
)