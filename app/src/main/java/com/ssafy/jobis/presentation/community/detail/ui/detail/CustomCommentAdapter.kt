package com.ssafy.jobis.presentation.community.detail.ui.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.community.Comment
import com.ssafy.jobis.databinding.CommentRecyclerBinding
import kotlinx.android.synthetic.main.comment_recycler.view.*
import java.text.SimpleDateFormat

class CustomCommentAdapter: RecyclerView.Adapter<CustomCommentAdapter.Holder>() {
    var listData = mutableListOf<Comment>()
    var uid = Firebase.auth.currentUser?.uid

    interface OnItemClickListener {
        fun onItemClick(v: View, comment: Comment, pos: Int)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CommentRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val comment = listData[position]
        holder.setComment(comment)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(val binding: CommentRecyclerBinding): RecyclerView.ViewHolder(binding.root) {
        fun setComment(comment: Comment) {
            val sdf = SimpleDateFormat("MM-dd hh:mm")
            binding.commentContentTextView.text = comment.content
            binding.commentNickNameTextView.text = comment.user_nickname
            binding.commentTimeTextView.text = sdf.format(comment.created_at.toDate()).toString()

            if (comment.user_id == uid) {
                binding.imageButton.visibility = View.VISIBLE

                binding.imageButton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        listener?.onItemClick(itemView, comment, pos)
                    }
                }
            }
        }
    }

}