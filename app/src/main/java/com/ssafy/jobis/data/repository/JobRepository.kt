package com.ssafy.jobis.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ssafy.jobis.data.model.calendar.CalendarDatabase
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.response.JobResponse
import kotlinx.coroutines.tasks.await

class JobRepository {
    suspend fun loadJobList(): MutableList<JobResponse> {
        val jobList = mutableListOf<JobResponse>()
        var db = FirebaseFirestore.getInstance()
        val jobRef = db.collection("schedules")
        return try {
            jobRef.get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        jobList.add(JobResponse.from(document.data as HashMap<String, Any>))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }
                .await()
            jobList
        } catch(e: Throwable) {
            e.printStackTrace()
            jobList
        }
    }

    fun insertSchedule(schedule: Schedule, context: Context): Schedule? {
        var db = CalendarDatabase.getInstance(context)
        var job: Schedule? = null
        val content = schedule.content
        val isAdded = db?.calendarDao()?.getSchedule(content)?.size!!
        if (isAdded > 0) {
            return job
        }
        var id = db?.calendarDao()?.insert(schedule)
        job = db?.calendarDao()?.getJob(id.toInt())
        return job
    }

    fun loadMyJobList(context: Context): List<Schedule>? {
        var db = CalendarDatabase.getInstance(context)
        return db?.calendarDao()?.getMyJob()
    }

    fun deleteSchedule(schedule: Schedule, context: Context): Boolean {
        var db = CalendarDatabase.getInstance(context)
        return try {
            db?.calendarDao()?.delete(schedule)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }
}