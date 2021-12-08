package com.ssafy.jobis.presentation.chat.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Chat

class ChatViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val chatName: TextView = view.findViewById(R.id.tv_chat_name)
    private val chatText: TextView = view.findViewById(R.id.tv_chat_msg)
    private val chatDate: TextView = view.findViewById(R.id.tv_chat_date)

    fun bind(chat: Chat, isSameTime: Boolean, nowTime: String, startChat: Boolean) {

        chatText.text = chat.content
        chatName.text = chat.nickname
        chatName.visibility = View.VISIBLE
        if(!startChat && isSameTime) chatName.visibility = View.GONE
        if (!isSameTime) chatDate.text = nowTime
    }
}