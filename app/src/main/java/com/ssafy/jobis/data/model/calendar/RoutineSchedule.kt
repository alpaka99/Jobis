package com.ssafy.jobis.data.model.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.*

@Entity
data class RoutineSchedule(
    @ColumnInfo var title: String,
    @ColumnInfo var content: String,
//    @ColumnInfo var year: Int,
//    @ColumnInfo var month: Int,
    @ColumnInfo var dayList: List<Calendar> ?= null,
    @ColumnInfo var startTime: String,
    @ColumnInfo var endTime: String,
    @ColumnInfo var groupId: Int,
    @ColumnInfo var companyName: String,
    @ColumnInfo var studyId: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

class Converters {
    @TypeConverter
    fun listToJson(value: List<Calendar>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value:String) = Gson().fromJson(value, Array<Calendar>::class.java).toList()
}
