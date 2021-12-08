package com.ssafy.jobis.presentation.admin.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.data.response.UserResponse
import com.ssafy.jobis.databinding.UserRecyclerBinding

class AdminUserAdapter: RecyclerView.Adapter<AdminUserAdapter.Holder>() {

    var listData = mutableListOf<UserResponse>()
    interface OnItemClickListener {
        fun onItemClick(v: View, user: UserResponse, pos: Int)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = UserRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = listData[position]
        holder.setUser(user)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(val binding: UserRecyclerBinding): RecyclerView.ViewHolder(binding.root) {
        fun setUser(user: UserResponse) {
            binding.userDeleteButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(itemView, user, pos)
                }
            }
            binding.adminEmailTextView.text = "이메일 : " + user.email
            binding.adminNicknameTextView.text = "넥네임 : " + user.nickname
            binding.adminPostTextView.text = "쓴글 개수: " + user.article_list.size.toString()
        }
    }
}