package com.ssafy.jobis.presentation.calendar

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.databinding.FragmentCalendarBinding
import com.ssafy.jobis.presentation.CalendarPagerAdapter
import com.ssafy.materialcalendar.OneDayDecorator
import com.ssafy.materialcalendar.SaturdayDecorator
import com.ssafy.materialcalendar.SundayDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.squareup.okhttp.Dispatcher
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.calendar.CalendarDatabase
import com.ssafy.jobis.data.model.calendar.RoutineSchedule
import com.ssafy.jobis.data.model.calendar.RoutineScheduleDatabase
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.data.response.ScheduleResponse
import com.ssafy.jobis.databinding.ActivityMainBinding
import com.ssafy.jobis.presentation.MainActivity
import com.ssafy.jobis.presentation.chat.adapter.ChatScheduleAdapter
import com.ssafy.jobis.presentation.chat.viewmodel.ChatScheduleViewModel
import com.ssafy.jobis.presentation.chat.viewmodel.ChatScheduleViewModelFactory
import com.ssafy.materialcalendar.EventDecorator
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.view.MotionEvent

import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.ssafy.jobis.data.model.study.Study


class CalendarFragment: Fragment(), OnMonthChangedListener, OnDateSelectedListener, CalendarScheduleAdapter.OnDeleteScheduleListener{
    private lateinit var chatScheduleViewModel: ChatScheduleViewModel
    private var currentDay: Int = 0
    private var currentMonth: Int = 0
    private var currentYear: Int = 0
    private var totalStudySchedule = ArrayList<Schedule>()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val uid = Firebase.auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        CoroutineScope(Dispatchers.Main).launch {
            val dialog = LoadingDialog(requireContext())
            dialog.show()
            // ??? ???????????? ???????????? ?????? ????????? id ????????????
            var study_id_list = getStudyIdList()
            var study_schedule_list = getStudyScheduleList(study_id_list)

            // ????????????2 ??????
            // ??? ???????????? ????????? ?????? ????????? ????????? ?????? ?????? ????????? ??????????????? ??????.
            var calc = Calendar.getInstance()

            var scheduleDatabase = CalendarDatabase.getInstance(context)
            var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(context)
            // ?????? ????????? ??????
            var firstYear = calc.get(Calendar.YEAR)
            var firstMonth = calc.get(Calendar.MONTH)
            var firstDay = calc.get(Calendar.DATE)

            // ??? ???????????? ?????? ?????????(??? ????????? ?????????)
            var viewPagerInfo = calculateCalendarDates(firstYear, firstMonth, firstDay, scheduleDatabase, routineScheduleDatabase, study_schedule_list)
            binding.calendarViewpager.adapter = CalendarPagerAdapter(viewPagerInfo, this@CalendarFragment) // ??? ????????? ???????????????
            dotDecorator(binding.calendarView, scheduleDatabase, routineScheduleDatabase, totalStudySchedule)
            selectedDate(firstDay) // ????????? ????????? ??????
            dialog.dismiss()
        }

        // ????????? ????????????
        var calendar = binding.calendarView
        var scheduleDatabase = CalendarDatabase.getInstance(this.context)
        var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(this.context)

        // 1. ??? ?????? ?????? "yyyy??? yy???"??? ????????????
        calendar.setTitleFormatter(TitleFormatter {
            val simpleDateFormat = SimpleDateFormat("yyyy??? MM???", Locale.KOREA)
            simpleDateFormat.format(Date().time)
        })

        // ?????? ????????? ???????????? ?????????
        calendar.setOnMonthChangedListener(this)

        // ?????? ???????????? ???????????? ?????????
        calendar.setOnDateChangedListener(this)

        // ?????????, ????????? ??????
        calendar.addDecorators(SundayDecorator(), SaturdayDecorator(), OneDayDecorator())


        // ????????????2 ??????
        // ??? ???????????? ????????? ?????? ????????? ????????? ?????? ?????? ????????? ??????????????? ??????.
        var calc = Calendar.getInstance()

        // ?????? ????????? ??????
        var firstYear = calc.get(Calendar.YEAR)
        var firstMonth = calc.get(Calendar.MONTH)
        var firstDay = calc.get(Calendar.DATE)


        // ?????? ???????????? ?????? ?????? = ?????? ??????, + ????????? ????????? ?????? = ?????? ??????
        binding.calendarView.setSelectedDate(calc)

        binding.calendarBtn.setOnClickListener {
            val intent = Intent(this.context, CalendarScheduleActivity::class.java)
            intent.putExtra("selected_year", firstYear)
            intent.putExtra("selected_month", firstMonth+1)
            intent.putExtra("selected_day", firstDay)
            startActivity(intent)
        }


        return binding.root
    }

    fun selectedDate(day: Int) {
        binding.calendarViewpager.currentItem = day-1 // ????????? ????????? ??????
    }
    suspend fun getStudyIdList() : ArrayList<String> {
        // ?????????????????? ????????????db?????? uid??? ????????? study??? id ????????????
        var study_id_list = ArrayList<String>()
        var study_info = FirebaseDatabase.getInstance().getReference("/Study")
        study_info.get().addOnSuccessListener {

//            val datas = it.value as HashMap<*, *>
//            for ((key, v) in datas) {
//                val data = v as HashMap<*, *>
//                var user_list = data["user_list"] as ArrayList<HashMap<*, *>>
//                for (k in 0..user_list.size-1) {
//                    if (uid == user_list[k]["id"]) {
//
//                        study_id_list.add(key.toString())
//                    }
//                }
//            }

            val mDatas = it.value as HashMap<*, *>
            for ((key, study) in mDatas) {
                if (study is Study) {
                    if (study.user_list == null) continue
                    for (userId in study.user_list!!) {
                        if (uid == userId.toString()) {
                            study_id_list.add(key.toString())
                        }
                    }
                }
            }


        }.await()
        return study_id_list
    }

    suspend fun getStudyScheduleList(study_id_list: ArrayList<String>) : ArrayList<Schedule> {
        // ?????? study_schdules ????????????
        var studyScheduleList = ArrayList<Schedule>()
        for (k in 0..study_id_list.size-1) {
            var currentId = study_id_list[k]
            var dbRef = Firebase.firestore
            var study_schedules = dbRef.collection("study_schedules").get()
            study_schedules.addOnSuccessListener {
                var studySchedules = it.documents
                for (i in 0..studySchedules.size-1) {
                    var studyId = studySchedules[i].get("study_id")
                    if (studyId == currentId) {
                        var year = studySchedules[i].get("year").toString().toInt()
                        var month = studySchedules[i].get("month").toString().toInt()
                        var day = studySchedules[i].get("day").toString().toInt()
                        var title = studySchedules[i].get("title").toString()
                        var content = studySchedules[i].get("content").toString()
                        var start_time = studySchedules[i].get("start_time").toString()
                        var end_time = studySchedules[i].get("end_time").toString()
                        var study_id = studySchedules[i].get("study_id").toString()
                        var group_id = studySchedules[i].get("group_id").toString().toInt()
                        var companyName = studySchedules[i].get("companyName").toString()
                        var schedule = Schedule(title, content, year, month, day, start_time, end_time, study_id, group_id, companyName)
                    studyScheduleList.add(schedule)
                    }
                }
            }.await()
        }
        return studyScheduleList
    }

    fun calculateCalendarDates(year : Int, month : Int, day : Int, scheduleDatabase: CalendarDatabase?, routineScheduleDatabase: RoutineScheduleDatabase?, studyScheduleList: ArrayList<Schedule>): ArrayList<ArrayList<Schedule>> {
        var calendarDates = ArrayList<ArrayList<Schedule>>()  // ??? ????????? ??????????????? ?????? ?????? List<Schedule>??? ????????? ?????? ArrayList
        val calc = Calendar.getInstance()
        calc.set(year, month, day)

        var lastDay = calc.getActualMaximum(Calendar.DAY_OF_MONTH)

        CoroutineScope(Dispatchers.IO).launch {
            var scheduleList = scheduleDatabase!!.calendarDao().getAll() // ?????? ?????? ???????????? [Schedule, Schedule, Schedule, ...]
            var routineList = routineScheduleDatabase!!.routineScheduleDao().getAll() // ?????? ?????? ????????????
            for (k in 1..lastDay) {

                var temp_schedule = ArrayList<Schedule>()

                // ?????? ?????? ??????
                if (routineList.size > 0) {
                    // i??? ?????? ?????? ??? ??????
                    for (i in 0..routineList.size-1) {
                        var title = routineList[i].title
                        var content = routineList[i].content
                        // j??? ??? ?????? ????????? ????????? ????????? ??????
                        for (j in 0..routineList[i].dayList!!.size-1) {
                            var routine_year = routineList[i].dayList!![j].get(Calendar.YEAR)
                            var routine_month = routineList[i].dayList!![j].get(Calendar.MONTH)
                            var routine_day = routineList[i].dayList!![j].get(Calendar.DATE)
                            if (year == routine_year && month == routine_month && k == routine_day) { // ?????? ?????? ????????? ?????? ?????? ????????? ????????????.
                                var startTime = routineList[i].startTime
                                var endTime = routineList[i].endTime
                                var studyId = routineList[i].studyId
                                var groupId = routineList[i].id
                                var companyName = routineList[i].companyName
                                var routine_schedule = Schedule(title, content, routine_year, routine_month, routine_day, startTime, endTime, studyId, groupId, companyName)
                                temp_schedule.add(routine_schedule)
                            }
                        }
                    }
                }

                // ???????????????????????? study ?????? ?????? ?????? ????????????
                if (studyScheduleList.size != 0) {
                    for (q in 0..studyScheduleList.size-1) {
                        if (studyScheduleList[q].year == year && studyScheduleList[q].month == month && studyScheduleList[q].day == k) {
                            temp_schedule.add(studyScheduleList[q])
                            totalStudySchedule.add(studyScheduleList[q])
                        }
                    }
                }

                // ?????? ?????? ??????
                // calendarDates??? ????????? ???????????? ????????? ?????? ??????
                // 1. ?????? ????????? ???????????? ?????? ???
                if (scheduleList.size > 0) {
                    for (j in 0..scheduleList.size-1) {
                        if (year == scheduleList[j].year && month == scheduleList[j].month && k == scheduleList[j].day) {
                            temp_schedule.add(scheduleList[j])
                        }
                    }
                }
                // 2. ?????? ????????? ?????? ???????????? ?????? ???, ?????? ????????? ????????? ????????? ?????????
                if (temp_schedule.size == 0) {
                    val empty_schedule = Schedule("?????? ??????", "", year, month, k, "", "", "", -1, "")
                    temp_schedule.add(empty_schedule)
                }
                calendarDates.add(temp_schedule) // ???????????? ???????????? ?????? ??????
            }
        }

        // coroutine??? ????????? ?????? ??????????????? ???????????? ??? ????????? ????????? ????????????..
        while (calendarDates.size == 0) {
        }

        return calendarDates
    }


    fun dotDecorator(calendar: MaterialCalendarView?, scheduleDatabase: CalendarDatabase?, routineScheduleDatabase: RoutineScheduleDatabase?, totalStudySchedule: ArrayList<Schedule>) {
        // ????????????1. room?????? ?????? ?????? ????????? ???????????? ???????????????
        var dates = ArrayList<CalendarDay>()

        CoroutineScope(Dispatchers.IO).launch {
            var scheduleList = scheduleDatabase!!.calendarDao().getAll()
            if (scheduleList.size != 0) { // schedule??? ?????? ??????..
                for (i in 0..scheduleList.size-1) {
                    var dot_year = scheduleList[i].year
                    var dot_month = scheduleList[i].month
                    var dot_day = scheduleList[i].day

                    // ??? ?????? => ?????? ?????? ??????????????? days??? ???????????? ??????????????? ??????..
                    // ????????? ????????? ?????? ????????????
                    var date = Calendar.getInstance()
                    date.set(dot_year, dot_month, dot_day)

                    // ????????? ????????? ?????? day List??? ??????
                    var day = CalendarDay.from(date) // Calendar ???????????? ???????????? ???
                    dates.add(day)

                    // ?????? ????????? ???????????? ????????? ???
//                    var date_for_text = ArrayList<CalendarDay>()
//                    date_for_text.add(day)
//                    calendar!!.addDecorator(TextDecorator(date_for_text, scheduleList[i].title))
                }
            }
        }

        // ????????????2. room?????? ?????? ?????? ????????? ???????????? ???????????????
        var routineDates = ArrayList<ArrayList<CalendarDay>>()
        CoroutineScope(Dispatchers.IO).launch {
            var routineScheduleList = routineScheduleDatabase!!.routineScheduleDao().getAll()
            for (i: Int in 0..routineScheduleList.size-1) {
                var dates = ArrayList<CalendarDay>() // ?????? ?????? ???????????? ????????? add??? ???????????? ???
                // ????????? ?????? ????????? ??????
                for (j: Int in 0..routineScheduleList[i].dayList!!.size-1) {
                    var routine = routineScheduleList[i].dayList!![j] // ??????
                    val routine_year = routine.get(Calendar.YEAR)
                    val routine_month = routine.get(Calendar.MONTH)
                    val routine_day = routine.get(Calendar.DAY_OF_MONTH)
                    var date = Calendar.getInstance()
                    date.set(routine_year, routine_month, routine_day)

                    var day = CalendarDay.from(date) // Calendar ???????????? ???????????? ???
                    dates.add(day)
                }
                routineDates.add(dates)
            }
        }

        // ????????????3. ???????????????????????? ???????????? ???????????????
        var study_dates = ArrayList<CalendarDay>()
        for (j: Int in 0..totalStudySchedule.size-1) {
            var study_year = totalStudySchedule[j].year
            var study_month = totalStudySchedule[j].month
            var study_day = totalStudySchedule[j].day
            var date = Calendar.getInstance()
            date.set(study_year, study_month, study_day)
            var day = CalendarDay.from(date)
            study_dates.add(day)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            calendar!!.removeDecorators()
            calendar!!.invalidateDecorators()
            calendar.addDecorators(SundayDecorator(), SaturdayDecorator(), OneDayDecorator())
            calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), dates)) // ??? ??????
            for (v: Int in 0..routineDates.size-1) {
                calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), routineDates[v])) // ??? ??????
            }
            calendar.addDecorator(EventDecorator(Color.parseColor("#ff4e7e"), study_dates))
        }, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }



    override fun onMonthChanged(widget: MaterialCalendarView?, date: CalendarDay?) {
        val dialog = LoadingDialog(requireContext())
        dialog.show()
        // ?????? ????????? ??? "yyyy??? yy???" ????????? ????????????
        widget?.setTitleFormatter(TitleFormatter {
            val simpleDateFormat = SimpleDateFormat("yyyy??? MM???", Locale.KOREA)
            simpleDateFormat.format(date?.date?.time)
        })
        // ??????????????? ????????? ????????? ?????? ?????? ???????????? ??????
        var year = date?.year
        var month = date?.month
        var day = date?.day

        var calc = Calendar.getInstance()
        calc.set(year!!, month!!, day!!)

        var scheduleDatabase = CalendarDatabase.getInstance(this.context)
        var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(this.context)

        CoroutineScope(Dispatchers.IO).launch {
            // ??? ???????????? ???????????? ?????? ????????? id ????????????
            var viewPagerInfo = ArrayList<ArrayList<Schedule>>()
            launch {
                var study_id_list = getStudyIdList()
                var study_schedule_list = getStudyScheduleList(study_id_list)

                // ??? ???????????? ?????? ?????????(??? ????????? ?????????)
               viewPagerInfo = calculateCalendarDates(
                    year,
                    month,
                    day,
                    scheduleDatabase,
                    routineScheduleDatabase,
                    study_schedule_list
                )
            }.join()
            withContext(Dispatchers.Main) {
            binding.calendarViewpager.adapter =
                CalendarPagerAdapter(viewPagerInfo, this@CalendarFragment) // ??? ????????? ???????????????


            binding.calendarViewpager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    calc.set(year, month, position + 1) // position??? 0?????? ??????, ????????? 1?????? ???????????????
                    // you are on the first page
                    binding.calendarView.setSelectedDate(calc)

                }
            })
                dialog.dismiss()
                dotDecorator(widget, scheduleDatabase, routineScheduleDatabase, totalStudySchedule)
            }
        }
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        currentDay = date.day
        currentMonth = date.month
        currentYear = date.year
        // ?????? 11????????? 10??????, 1????????? 0?????? ????????????. Month??? +1??? ?????? ???????????? ???
        var selectedDay = date.day
        var selectedMonth = date.month + 1
        var selectedYear = date.year
        if (selected) {
            selectedDate(selectedDay) // ????????? ????????? ??????

            binding.calendarBtn.setOnClickListener {
                val intent = Intent(this.context, CalendarScheduleActivity::class.java)
                intent.putExtra("selected_year", selectedYear)
                intent.putExtra("selected_month", selectedMonth)
                intent.putExtra("selected_day", selectedDay)
                startActivity(intent)
            }
        }
    }

    override fun onDeleteSchedule(schedule: Schedule) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(this.context, R.style.ThemeOverlay_AppCompat_Dialog))
        if (schedule.study_id != "") {
            builder.setTitle("??????")
            builder.setMessage("????????? ????????? ?????? ??????????????? ????????? ??? ????????????.")
            builder.setPositiveButton("??????", DialogInterface.OnClickListener { dialog, which ->
            })
        } else {
            builder.setTitle("??????")
            builder.setMessage("????????? ?????????????????????????")
            builder.setPositiveButton("??????", DialogInterface.OnClickListener { dialog, which ->
                CoroutineScope(Dispatchers.IO).launch {
                    launch {
                        if (schedule.group_id == 0) {
                            var scheduleData = CalendarDatabase.getInstance(requireContext())
                            scheduleData!!.calendarDao().delete(schedule)
                        } else {
                            var routineScheduleData =
                                RoutineScheduleDatabase.getInstance(requireContext())
                            routineScheduleData!!.routineScheduleDao()
                                .deleteRoutineSchedules(schedule.group_id)
                            Log.d("test", "??????")
                        }
                    }.join()
                    Log.d("test2", "??????2")
                    updateAdapter()
                }
            })
            builder.setNegativeButton("??????") {
                    _, _ -> println("?????? ??????")
            }
        }

        builder.show()
    }

    fun updateAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            // ??? ???????????? ???????????? ?????? ????????? id ????????????
            var study_id_list = getStudyIdList()
            var study_schedule_list = getStudyScheduleList(study_id_list)

            // ????????????2 ??????
            // ??? ???????????? ????????? ?????? ????????? ????????? ?????? ?????? ????????? ??????????????? ??????.
            var calc = Calendar.getInstance()
            calc.set(currentYear, currentMonth, currentDay)

            var scheduleDatabase = CalendarDatabase.getInstance(context)
            var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(context)
            // ?????? ????????? ??????
            var firstYear = calc.get(Calendar.YEAR)
            var firstMonth = calc.get(Calendar.MONTH)
            var firstDay = calc.get(Calendar.DATE)

            // ??? ???????????? ?????? ?????????(??? ????????? ?????????)
            var viewPagerInfo = calculateCalendarDates(firstYear, firstMonth, firstDay, scheduleDatabase, routineScheduleDatabase, study_schedule_list)
            binding.calendarViewpager.adapter = CalendarPagerAdapter(viewPagerInfo, this@CalendarFragment) // ??? ????????? ???????????????
            dotDecorator(binding.calendarView, scheduleDatabase, routineScheduleDatabase, totalStudySchedule)

            selectedDate(currentDay) // ????????? ????????? ??????
        }
    }
    private fun showLoadingDialog() {
        val dialog = LoadingDialog(this.requireContext())
        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            delay(3000)
            dialog.dismiss()
        }
    }
}