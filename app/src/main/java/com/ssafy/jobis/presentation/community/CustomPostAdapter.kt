package com.ssafy.jobis.presentation.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.databinding.PostRecyclerBinding
import java.text.SimpleDateFormat
import java.util.*

class CustomPostAdapter: RecyclerView.Adapter<CustomPostAdapter.Holder>() {

    var listData = mutableListOf<PostResponse>()

    interface OnItemClickListener{
        fun onItemClick(v: View, post: PostResponse, pos: Int)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomPostAdapter.Holder {
        val binding = PostRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: CustomPostAdapter.Holder, position: Int) {
        val post = listData.get(position)
        holder.setPost(post)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(val binding: PostRecyclerBinding): RecyclerView.ViewHolder(binding.root) {
        fun setPost(post: PostResponse) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val cal = Calendar.getInstance()
            cal.time = post.created_at.toDate()
            cal.add(Calendar.HOUR, 9)
            binding.postCategoryText.text = post.category
            binding.postCommentText.text = "댓글 " + post.comment_list.size.toString()
            binding.postContentText.text = post.content
            binding.postLikeText.text = "좋아요 " + post.like.size.toString()
            binding.postTimeText.text = sdf.format(cal.time).toString()
//            binding.postTimeText.text = sdf.format(post.created_at.toDate())
            binding.postTitleText.text = post.title

            val pos = adapterPosition
            if (pos!=RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView, post, pos)
                }
            }
        }
    }

}