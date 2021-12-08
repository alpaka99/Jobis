package com.ssafy.jobis.presentation.chat.viewholder

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.chat.ImgChat
import com.ssafy.jobis.presentation.chat.adapter.ChatAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatGIFViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val name: TextView = view.findViewById(R.id.tv_gif_name)
    private val img: ImageView = view.findViewById(R.id.img_gif)
    private val date: TextView = view.findViewById(R.id.tv_gif_date)

    fun bind(imgChat: ImgChat?, nickname: String, isSameTime: Boolean, nowTime: String, startChat: Boolean) {
        if (imgChat == null) {
            img.visibility = View.GONE
            return
        }
        img.visibility = View.VISIBLE

        name.text = nickname
        name.visibility = View.VISIBLE
        if (!startChat && isSameTime) name.visibility = View.GONE
        if (!isSameTime) date.text = nowTime


        val drawable = imgChat.drawable

        img.setImageDrawable(drawable)
        if (!imgChat.isMoved && drawable  is AnimatedImageDrawable) {
            drawable.repeatCount = 4
            drawable.start()
            imgChat.isMoved = true
        }
        if (drawable  is AnimatedImageDrawable) {
            img.setOnClickListener {
                drawable.repeatCount = 4
                drawable.start()
            }
        }
    }
}