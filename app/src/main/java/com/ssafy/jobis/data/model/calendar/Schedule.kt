package com.ssafy.jobis.data.model.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Schedule (
    @ColumnInfo var title: String,
    @ColumnInfo var content: String,
    // 날짜
    @ColumnInfo var year: Int,
    @ColumnInfo var month: Int,
    @ColumnInfo var day: Int,
    // 일정 시작 시간
    @ColumnInfo var start_time: String,
    @ColumnInfo var end_time: String,
    // 일정이 스터디에서 만든 일정이라면 studyId를 추가
    @ColumnInfo var study_id: String,
    // 일정이 주간일정이라면 groupId를 추가
    @ColumnInfo var group_id: Int,
    @ColumnInfo var companyName: String


    ) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}