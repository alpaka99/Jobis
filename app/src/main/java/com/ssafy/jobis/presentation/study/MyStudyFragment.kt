package com.ssafy.jobis.presentation.study

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.databinding.FragmentMyStudyBinding
import com.ssafy.jobis.presentation.study.adapter.MyStudyAdapter
import java.text.SimpleDateFormat
import java.util.*

class MyStudyFragment(val myContext: Context): Fragment() {

    private var _binding: FragmentMyStudyBinding? = null
    private val binding get() = _binding!!

    private var isFabOpen = false

    private lateinit var viewModel: StudyViewModel
    private lateinit var studyAdapter: MyStudyAdapter

    private var dDayList: MutableList<Int?> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMyStudyBinding.inflate(inflater, container, false)

        activity?.run {
            viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(this.application)
            ).get(StudyViewModel::class.java)

        }
        isFabOpen = false

        viewModel.studyList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) binding.txtStudyBlank.visibility = View.GONE
            fetchSchedule()
            studyAdapter = MyStudyAdapter(myContext, it, dDayList )
            binding.rvMyStudy.adapter = studyAdapter
        })



        binding.fabMyStudy.setOnClickListener {
            toggleFab()
        }

        binding.fabCreateStudy.setOnClickListener {
            isFabOpen = !isFabOpen
            val intent = Intent(context, CreateStudy::class.java)
            startActivity(intent)
        }

        binding.fabSearchStudy.setOnClickListener {
            isFabOpen = !isFabOpen
            val intent = Intent(context, SearchStudy::class.java)
            startActivity(intent)
        }



        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun toggleFab() {

        if (isFabOpen) {
            ObjectAnimator.ofFloat(binding.fabSearchStudy, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabCreateStudy, "translationY", 0f).apply { start() }
            binding.fabMyStudy.setImageResource(R.drawable.ic_add_24)
        } else {
            ObjectAnimator.ofFloat(binding.fabSearchStudy, "translationY", -130f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabCreateStudy, "translationY", -260f).apply { start() }
            binding.fabMyStudy.setImageResource(R.drawable.ic_close_24)
        }

        isFabOpen = !isFabOpen
    }

    private fun fetchSchedule() {
        dDayList.clear()
        val firestore = FirebaseFirestore.getInstance()

        for (i in 0..viewModel.studyList.value!!.size-1){
            dDayList.add(null)
        }

        var study_schedules = firestore.collection("study_schedules").get()
        study_schedules.addOnSuccessListener {
            var studySchedules = it.documents
            for (i in 0..studySchedules.size - 1) {
//                var studyId = studySchedules[i].get("study_id")
                var title = studySchedules[i].get("title").toString()
                var content = studySchedules[i].get("content").toString()
                // 날짜
                var year = studySchedules[i].get("year").toString().toInt()
                var month = studySchedules[i].get("month").toString().toInt()
                var day = studySchedules[i].get("day").toString().toInt()
                // 일정 시작 시간
                var start_time = studySchedules[i].get("start_time").toString()
                var end_time = studySchedules[i].get("end_time").toString()
                // 일정이 스터디에서 만든 일정이라면 studyId를 추가
                var study_id = studySchedules[i].get("study_id").toString()
                // 일정이 주간일정이라면 groupId를 추가
                var group_id = studySchedules[i].get("group_id").toString().toInt()
                var companyName = studySchedules[i].get("companyName").toString()

                val tmpSchedule = Schedule(
                    title = title,
                    content = content,
                    year = year,
                    month = month,
                    day = day,
                    start_time = start_time,
                    end_time = end_time,
                    study_id = study_id,
                    group_id = group_id,
                    companyName = companyName
                )

//                 여기서 d-day를 판단함
                Log.i("schedule", tmpSchedule.toString())
                Log.i("study", viewModel.studyList.value!!.toString())
                for (i in 0..viewModel.studyList.value!!.size-1) {
                    if (tmpSchedule.study_id == viewModel.studyList.value!![i].id) {
                        // 날짜만 판단
                        val cal = Calendar.getInstance()
                        cal.time = Date()
                        val df_month = SimpleDateFormat("MM")
                        val df_day = SimpleDateFormat("dd")
                        val this_day = df_day.format(cal.time).toString()
                        val this_month = df_month.format(cal.time).toString()

                        var d_day = 0


                        // 아 날짜 계산 어캐하지..
                        d_day = day - this_day.toInt()

                        if (d_day <= 0){
                            d_day += (this_month.toInt() - (month+1))*30
                        }

                        if (dDayList[i] == null) {
                            dDayList[i] = d_day
                        } else {
                            if (dDayList[i]!! > d_day) {
                                dDayList[i] = d_day
                            }
                        }
                    }
                }
                Log.i("final", dDayList.toString())
            }
            studyAdapter.notifyDataSetChanged()
        }
    }
}