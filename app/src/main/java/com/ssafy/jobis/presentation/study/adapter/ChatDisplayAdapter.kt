package com.ssafy.jobis.presentation.study.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Chat

class ChatDisplayAdapter(val chatList: List<Chat>): RecyclerView.Adapter<ChatDisplayAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatDisplayAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tmp_list_item, parent, false)

        return CustomViewHolder(view).apply {
            itemView.setOnClickListener{
                val curPos:Int = adapterPosition
                val chat: Chat = chatList.get(curPos)
                Toast.makeText(parent.context, "이름 : ${chat.content}", Toast.LENGTH_SHORT).show()

            }

        }
    }

    override fun onBindViewHolder(holder: ChatDisplayAdapter.CustomViewHolder, position: Int) {
        holder.name.text = chatList.get(position).content
        holder.uid.text = chatList.get(position).user_id
        holder.describe.text = chatList.get(position).created_at

        Log.d("holder", holder.toString())
    }

    override fun getItemCount(): Int {
        return chatList.size
//        return 5
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tv_studyName)        // 이름
        val uid = itemView.findViewById<TextView>(R.id.tv_uid)          // 나이
        val describe = itemView.findViewById<TextView>(R.id.tv_studyDescribe)          // 직업

    }



}