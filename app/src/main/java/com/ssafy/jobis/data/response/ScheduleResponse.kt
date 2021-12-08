package com.ssafy.jobis.data.response

data class ScheduleResponse(
    val companyName: String,
    val content: String,
    val year: Long,
    val month: Long,
    val day: Long,
    val start_time: String,
    val end_time: String,
    val group_id: Long,
    val study_id: String,
    val title: String,
) {
    companion object {
        fun from(map: HashMap<String, Any>): ScheduleResponse {
            return object {
                val companyName by map
                val content by map
                val year by map
                val month by map
                val day by map
                val start_time by map
                val end_time by map
                val group_id by map
                val study_id by map
                val title by map
                val data = ScheduleResponse(
                    companyName as String,
                    content as String,
                    year as Long,
                    month as Long,
                    day as Long,
                    start_time as String,
                    end_time as String,
                    group_id as Long,
                    study_id as String,
                    title as String
                )
            }.data
        }
    }
}