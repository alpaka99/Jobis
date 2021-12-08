package com.ssafy.jobis.presentation.study.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.presentation.study.viewholder.MyStudyViewHolder

class MyStudyAdapter(val context: Context, val studyList: List<Study>, val dDayList: MutableList<Int?>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyStudyViewHolder {
        return MyStudyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_study, parent, false), context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val study = studyList[position]
        val dDay = dDayList[position]
        if (holder is MyStudyViewHolder)
            holder.bind(study, dDay)
    }

    override fun getItemCount(): Int {
        return  studyList.size
    }

}