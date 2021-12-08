package com.ssafy.jobis.presentation.chat.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Chat

class ChatMyViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val chatText: TextView = view.findViewById(R.id.tv_my_chat_msg)
    private val chatDate: TextView = view.findViewById(R.id.tv_my_chat_date)

    fun bind(chat: Chat, isSameTime: Boolean, nowTime: String) {

        chatText.text = chat.content
        if (!isSameTime) chatDate.text = nowTime
    }
}