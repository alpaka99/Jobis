package com.ssafy.jobis.presentation.chat.adapter

import android.app.Activity
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Chat
import com.ssafy.jobis.presentation.chat.ImgChat
import com.ssafy.jobis.presentation.chat.viewholder.*
import java.util.HashMap

class ChatAdapter(val uid: String, val chatList: List<Chat>?, val map: HashMap<Int, ImgChat?>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface onAddedChatListener {
        fun onAddedChat()
    }

    val CHAT_MY_ITEM = 0
    val CHAT_ITEM = 1
    val GIF_ITEM = 2
    val GIF_MY_ITEM = 3
    val ENTRANCE = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CHAT_MY_ITEM -> ChatMyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itme_chat_me, parent, false))
            CHAT_ITEM -> ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
            GIF_MY_ITEM -> ChatMyGIFViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_gif_me, parent, false))
            GIF_ITEM -> ChatGIFViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_gif, parent, false))
            ENTRANCE -> ChatEntranceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_entrance, parent, false))
            else -> ChatMyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itme_chat_me, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (chatList == null) return
        var isSameTime = false
        val (nowDate, noon, nowTime) = chatList[position].created_at.split(" ")
        val nowUid = chatList[position].user_id

        if (position < chatList.size-1) {
            val (postDate, noon, postTime) = chatList[position+1].created_at.split(" ")
            val postUid = chatList[position+1].user_id
            if ( nowTime == postTime && nowUid == postUid && nowDate == postDate) {
                isSameTime = true
            }
        }

        var startChat = true
        if (0 < position && chatList[position-1].user_id == chatList[position].user_id)
            startChat = false

        val time = "$noon $nowTime"

        when (holder) {
            is ChatEntranceViewHolder -> holder.bind(chatList[position])
            is ChatMyViewHolder -> holder.bind(chatList[position], isSameTime, time)
            is ChatViewHolder -> holder.bind(chatList[position], isSameTime, time, startChat)
            is ChatMyGIFViewHolder -> holder.bind(map[position], isSameTime, time)
            is ChatGIFViewHolder -> holder.bind(map[position], chatList[position].nickname, isSameTime, time, startChat)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (chatList != null) {
            return if (chatList[position].is_entrance) {
                ENTRANCE
            } else if (chatList[position].file_name.isNotEmpty()) {
                if (chatList[position].is_me) GIF_MY_ITEM
                else GIF_ITEM
            } else {
                if (chatList[position].is_me) CHAT_MY_ITEM
                else CHAT_ITEM
            }
        }
        return 0
    }

    override fun getItemCount(): Int {
        return chatList?.size?:0
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.scrollToPosition(itemCount-1)
    }


//    fun addChat(isGif: Boolean, source: ImageDecoder.Source?, content: String?) {
//        val drawable = if (source != null) ImageDecoder.decodeDrawable(source!!) else null
//        // ???????????????????????? ????????? ???????????? ???????????? ????????? ???????????? ?????? ?????? ?????? ??? ????????? ???????????? ?????????
//        // ??? ???????????? gif????????? ?????? ?????????????????? ???????????? boolean?????? ?????? ??????
//        // ????????? ???????????? ????????? ?????? ??? ????????? ???????????? ????????? gif????????? ???????????? ?????? ??????.
//        // ?????????, ???????????? ????????? ????????? ???????????? drawable ????????? ????????? ???????????? ???????????? ?????? ?????? ???????????? ??????????????????
//        // ???????????? ????????? ???????????? ??????.
//        chatList.add(StudyChat(isGif, drawable, content))
//        notifyDataSetChanged()
//        if (activity is onAddedChatListener) {
//            activity.onAddedChat()
//        }
//    }

//    data class StudyChat(
//        val isGif: Boolean = false,
//        val drawable: Drawable? = null,
//        val content: String? = null,
//        var isMoved: Boolean = false
//    )
}