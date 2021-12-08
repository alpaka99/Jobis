package com.ssafy.jobis.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.data.model.calendar.OnDeleteClick
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.databinding.CalendarViewpagerBinding
import com.ssafy.jobis.presentation.calendar.CalendarFragment
import com.ssafy.jobis.presentation.calendar.CalendarScheduleAdapter
import java.util.*
import kotlin.collections.ArrayList

class CalendarPagerViewHolder(val binding: CalendarViewpagerBinding) :
        RecyclerView.ViewHolder(binding.root)

class CalendarPagerAdapter(private var dates: ArrayList<ArrayList<Schedule>>, private var frag: CalendarFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder = CalendarPagerViewHolder(CalendarViewpagerBinding.inflate(
        LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CalendarPagerViewHolder).binding
        // 뷰에 데이터 출력
        var txt = (dates[position][0].month+1).toString() + "월 " + dates[position][0].day.toString() + "일"
        binding.calendarViewDate.text = txt


        if (dates[position][0].title == "일정 없음") {
            binding.emptyText.layoutParams.height = 300
            binding.emptyText.text = "예정된 일정이 없습니다."
            binding.viewpagerContentRecycler.adapter = CalendarScheduleAdapter(ArrayList<Schedule>(), frag)
        } else {
            binding.emptyText.layoutParams.height = 0
            binding.viewpagerContentRecycler.adapter = CalendarScheduleAdapter(dates[position], frag)
        }

        // 구분선 추가
        val dividerItemDecoration = DividerItemDecoration(binding.viewpagerContentRecycler.context , LinearLayoutManager.VERTICAL);
        binding.viewpagerContentRecycler.addItemDecoration(dividerItemDecoration)
    }

    override fun getItemCount(): Int {
        return dates.size
    }
}