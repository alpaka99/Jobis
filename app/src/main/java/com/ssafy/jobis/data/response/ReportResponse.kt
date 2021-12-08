package com.ssafy.jobis.data.response

data class ReportResponse(
    val post_id: String,
    val reason: String,
    val reporter_id: String,
    val id: String
) {
    companion object {
        fun from(map: Map<String, Any>, report_id: String): ReportResponse {
            return object {
                val post_id by map
                val reason by map
                val reporter_id by map
                val data = ReportResponse(
                    post_id as String,
                    reason as String,
                    reporter_id as String,
                    id=report_id
                )
            }.data
        }
    }
}