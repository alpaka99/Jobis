package com.ssafy.jobis.presentation.chat.viewholder

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Chat

class ChatEntranceViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val content: TextView = view.findViewById(R.id.tv_entrance_content)

    fun bind(chat: Chat) {
        content.text = chat.content
    }
}