package com.ssafy.jobis.presentation.chat.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.presentation.chat.ChatScheduleActivity
import com.ssafy.jobis.presentation.chat.viewholder.ChatScheduleViewHolder

class ChatScheduleAdapter(private val datas: ArrayList<Schedule>, private val chatScheduleActivity: ChatScheduleActivity) :
    RecyclerView.Adapter<ChatScheduleViewHolder>() {

    interface OnDeleteStudyScheduleListener {
        fun onDeleteStudySchedule(schedule: Schedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatScheduleViewHolder {
        var empty = chatScheduleActivity.findViewById<TextView>(R.id.empty_study_schedule)
        if (datas.size == 0) {
            println("working")
            empty.layoutParams.height = 300
        } else {
            println("not working")
            empty.layoutParams.height = 0
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_study_schedule, parent, false)
        return ChatScheduleViewHolder(view, chatScheduleActivity)
    }
    override fun onBindViewHolder(holder: ChatScheduleViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    override fun getItemCount(): Int {
        return datas.size
    }

}