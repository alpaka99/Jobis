package com.ssafy.jobis.presentation.job

import android.text.TextUtils.split
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.response.JobResponse
import com.ssafy.jobis.databinding.JobRecyclerBinding
import com.ssafy.jobis.databinding.JobScheduleRecyclerBinding
import java.text.SimpleDateFormat
import java.util.*


class JobScheduleAdapter: RecyclerView.Adapter<JobScheduleAdapter.Holder>() {

    var listData = listOf<Schedule>()
    private var listener: OnItemClickListener? = null
    interface OnItemClickListener{
        fun onItemClick(v: View, schedule: Schedule, post: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = JobScheduleRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val schedule = listData.get(position)
        holder.setSchedule(schedule)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(val binding: JobScheduleRecyclerBinding): RecyclerView.ViewHolder(binding.root) {
        fun setSchedule(schedule: Schedule) {
            binding.jobScheduleTitleTextView.text = schedule.companyName
            binding.jobScheduleContentTextView.text = schedule.content

            val deadline = getDDay(schedule.year, schedule.month+1, schedule.day).toInt()
            if (deadline < 0) {
                binding.scheduleDueTextView.text = "마감된 공고"
            } else if (deadline == 0) {
                binding.scheduleDueTextView.text = "오늘마감"
            } else {
                binding.scheduleDueTextView.text = "D - ${deadline}"
            }

            val pos = adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView, schedule, pos)
                }
            }

        }

        private fun getDDay(year: Int, month: Int, day: Int): Long {

            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val now_time = sdf.format(Date()).split("-")
            val nowDay = Calendar.getInstance().apply {
                set(Calendar.YEAR, now_time[0].toInt())
                set(Calendar.MONTH, now_time[1].toInt())
                set(Calendar.DAY_OF_MONTH, now_time[2].toInt())
            }.timeInMillis

            val dueDay = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }.timeInMillis

            val dDay = getIgnoredTimeDays(dueDay) - getIgnoredTimeDays(nowDay)
            return dDay / (24*60*60*1000)
        }

        private fun getIgnoredTimeDays(time: Long): Long {
            return Calendar.getInstance().apply {
                timeInMillis = time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
}