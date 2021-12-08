package com.ssafy.jobis.presentation.chat.viewholder

import android.graphics.drawable.AnimatedImageDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.chat.ImgChat

class ChatMyGIFViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val img: ImageView = view.findViewById(R.id.img_my_gif)
    private val date: TextView = view.findViewById(R.id.tv_my_gif_date)

    fun bind(imgChat: ImgChat?, isSameTime: Boolean, nowTime: String) {
        if (imgChat == null) {
            img.visibility = View.GONE
            return
        }
        img.visibility = View.VISIBLE

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