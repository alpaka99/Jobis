package com.ssafy.jobis.presentation.study.viewholder

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.presentation.chat.ChatActivity

class MyStudyViewHolder(val view: View, val context: Context): RecyclerView.ViewHolder(view) {


    private val myStudyTitle: TextView = view.findViewById(R.id.tv_my_study_title)
    private val myStudyMsg: TextView = view.findViewById(R.id.tv_my_study_msg)
    private val myStudyDate: TextView = view.findViewById(R.id.tv_my_study_date)
    private val myStudyMsgCnt: TextView = view.findViewById(R.id.tv_my_study_msg_cnt)
    private val myStudyDDay: TextView = view.findViewById(R.id.tv_my_study_dday)
    private val myStudyPerson: TextView = view.findViewById(R.id.tv_my_study_person)

    fun bind(study: Study, dDay: Int?) {

        if (study.unread_chat_cnt == 0) {
            myStudyMsgCnt.visibility = View.GONE
        } else {
            myStudyMsgCnt.text = study.unread_chat_cnt.toString()
            myStudyMsgCnt.visibility = View.VISIBLE
        }

        myStudyPerson.text = study.current_user.toString()+"/"+study.max_user.toString()
        myStudyMsg.text = study.last_chat

        if (study.last_date != "") {
            val (date, noon, time) = study.last_date.split(" ")
            myStudyDate.text = "$noon $time"
        }

        myStudyTitle.text = study.title
        if (dDay == null) {
            myStudyDDay.text = "D-day"
        }
        else {
            myStudyDDay.text = "D-${dDay.toString()}"
        }
        view.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("study_id", study.id)
            intent.putExtra("study_title", study.title)
            context.startActivity(intent)
        }
    }
}