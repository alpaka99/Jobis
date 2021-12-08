package com.ssafy.jobis.data.response

import com.google.common.math.IntMath

data class JobResponse(
    val companyName: String,
    val content: String,
    val year: Long,
    val month: Long,
    val day: Long,
    val end_time: String,
    val groupId: Long,
    val studyId: String,
    val title: String,
) {
    companion object {
        fun from(map: HashMap<String, Any>): JobResponse {
            return object {
                val companyName by map
                val content by map
                val year by map
                val month by map
                val day by map
                val end_time by map
                val title by map
                val data = JobResponse(
                    companyName as String,
                    content as String,
                    year as Long,
                    month as Long,
                    day as Long,
                    end_time as String,
                    groupId = 0,
                    studyId = "",
                    title as String
                )
            }.data
        }
    }
}
