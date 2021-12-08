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
            // 내 아이디를 포함하고 있는 스터디 id 가져오기
            var study_id_list = getStudyIdList()
            var study_schedule_list = getStudyScheduleList(study_id_list)

            // 뷰페이저2 사용
            // 첫 화면에서 보여줄 달의 정보를 가지고 있는 뷰를 여기서 만들어줘야 한다.
            var calc = Calendar.getInstance()

            var scheduleDatabase = CalendarDatabase.getInstance(context)
            var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(context)
            // 처음 보여줄 날짜
            var firstYear = calc.get(Calendar.YEAR)
            var firstMonth = calc.get(Calendar.MONTH)
            var firstDay = calc.get(Calendar.DATE)

            // 뷰 페이저에 넣을 내용들(한 달간의 일정들)
            var viewPagerInfo = calculateCalendarDates(firstYear, firstMonth, firstDay, scheduleDatabase, routineScheduleDatabase, study_schedule_list)
            binding.calendarViewpager.adapter = CalendarPagerAdapter(viewPagerInfo, this@CalendarFragment) // 뷰 페이저 만들어주기
            dotDecorator(binding.calendarView, scheduleDatabase, routineScheduleDatabase, totalStudySchedule)
            selectedDate(firstDay) // 선택한 날짜로 이동
            dialog.dismiss()
        }

        // 캘린더 레이아웃
        var calendar = binding.calendarView
        var scheduleDatabase = CalendarDatabase.getInstance(this.context)
        var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(this.context)

        // 1. 맨 처음 달력 "yyyy년 yy월"로 표기하기
        calendar.setTitleFormatter(TitleFormatter {
            val simpleDateFormat = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
            simpleDateFormat.format(Date().time)
        })

        // 달력 넘기면 동작하는 리스너
        calendar.setOnMonthChangedListener(this)

        // 날짜 선택하면 동작하는 리스너
        calendar.setOnDateChangedListener(this)

        // 토요일, 일요일 색칠
        calendar.addDecorators(SundayDecorator(), SaturdayDecorator(), OneDayDecorator())


        // 뷰페이저2 사용
        // 첫 화면에서 보여줄 달의 정보를 가지고 있는 뷰를 여기서 만들어줘야 한다.
        var calc = Calendar.getInstance()

        // 처음 보여줄 날짜
        var firstYear = calc.get(Calendar.YEAR)
        var firstMonth = calc.get(Calendar.MONTH)
        var firstDay = calc.get(Calendar.DATE)


        // 처음 선택되어 있는 날짜 = 현재 날짜, + 버튼에 연결된 날짜 = 현재 날짜
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
        binding.calendarViewpager.currentItem = day-1 // 선택한 날짜로 이동
    }
    suspend fun getStudyIdList() : ArrayList<String> {
        // 파이어베이스 리얼타임db에서 uid가 포함된 study의 id 가져오기
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
        // 모든 study_schdules 가져오기
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
        var calendarDates = ArrayList<ArrayList<Schedule>>()  // 각 날짜의 스케줄들을 담고 있는 List<Schedule>을 원소로 하는 ArrayList
        val calc = Calendar.getInstance()
        calc.set(year, month, day)

        var lastDay = calc.getActualMaximum(Calendar.DAY_OF_MONTH)

        CoroutineScope(Dispatchers.IO).launch {
            var scheduleList = scheduleDatabase!!.calendarDao().getAll() // 모든 일정 가져오기 [Schedule, Schedule, Schedule, ...]
            var routineList = routineScheduleDatabase!!.routineScheduleDao().getAll() // 모든 루틴 가져오기
            for (k in 1..lastDay) {

                var temp_schedule = ArrayList<Schedule>()

                // 반복 일정 처리
                if (routineList.size > 0) {
                    // i는 반복 일정 한 세트
                    for (i in 0..routineList.size-1) {
                        var title = routineList[i].title
                        var content = routineList[i].content
                        // j는 각 반복 일정의 날짜를 구하기 위함
                        for (j in 0..routineList[i].dayList!!.size-1) {
                            var routine_year = routineList[i].dayList!![j].get(Calendar.YEAR)
                            var routine_month = routineList[i].dayList!![j].get(Calendar.MONTH)
                            var routine_day = routineList[i].dayList!![j].get(Calendar.DATE)
                            if (year == routine_year && month == routine_month && k == routine_day) { // 이번 달의 일정일 때만 아래 동작을 수행한다.
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

                // 파이어베이스에서 study 단일 일정 모두 가져오기
                if (studyScheduleList.size != 0) {
                    for (q in 0..studyScheduleList.size-1) {
                        if (studyScheduleList[q].year == year && studyScheduleList[q].month == month && studyScheduleList[q].day == k) {
                            temp_schedule.add(studyScheduleList[q])
                            totalStudySchedule.add(studyScheduleList[q])
                        }
                    }
                }

                // 단일 일정 처리
                // calendarDates의 원소를 하나하나 만들어 넣을 것임
                // 1. 해당 날짜에 스케줄이 있을 때
                if (scheduleList.size > 0) {
                    for (j in 0..scheduleList.size-1) {
                        if (year == scheduleList[j].year && month == scheduleList[j].month && k == scheduleList[j].day) {
                            temp_schedule.add(scheduleList[j])
                        }
                    }
                }
                // 2. 해당 날짜에 아무 스케줄이 없을 때, 일정 없음이 표시된 객체를 넣어줌
                if (temp_schedule.size == 0) {
                    val empty_schedule = Schedule("일정 없음", "", year, month, k, "", "", "", -1, "")
                    temp_schedule.add(empty_schedule)
                }
                calendarDates.add(temp_schedule) // 하루하루 일정들을 모두 추가
            }
        }

        // coroutine이 끝나고 나서 뷰페이저를 구성해야 빈 화면이 보이지 않으므로..
        while (calendarDates.size == 0) {
        }

        return calendarDates
    }


    fun dotDecorator(calendar: MaterialCalendarView?, scheduleDatabase: CalendarDatabase?, routineScheduleDatabase: RoutineScheduleDatabase?, totalStudySchedule: ArrayList<Schedule>) {
        // 사전작업1. room에서 단일 일정 데이터 가져와서 표시해주기
        var dates = ArrayList<CalendarDay>()

        CoroutineScope(Dispatchers.IO).launch {
            var scheduleList = scheduleDatabase!!.calendarDao().getAll()
            if (scheduleList.size != 0) { // schedule이 있을 때만..
                for (i in 0..scheduleList.size-1) {
                    var dot_year = scheduleList[i].year
                    var dot_month = scheduleList[i].month
                    var dot_day = scheduleList[i].day

                    // 점 찍기 => 여러 날에 표시하려고 days를 구성해서 추가해주는 방식..
                    // 달력에 표시할 날짜 가져오기
                    var date = Calendar.getInstance()
                    date.set(dot_year, dot_month, dot_day)

                    // 달력에 표시할 날짜 day List에 넣기
                    var day = CalendarDay.from(date) // Calendar 자료형을 넣어주면 됨
                    dates.add(day)

                    // 글자 표시는 하루하루 해줘야 함
//                    var date_for_text = ArrayList<CalendarDay>()
//                    date_for_text.add(day)
//                    calendar!!.addDecorator(TextDecorator(date_for_text, scheduleList[i].title))
                }
            }
        }

        // 사전작업2. room에서 반복 일정 데이터 가져와서 표시해주기
        var routineDates = ArrayList<ArrayList<CalendarDay>>()
        CoroutineScope(Dispatchers.IO).launch {
            var routineScheduleList = routineScheduleDatabase!!.routineScheduleDao().getAll()
            for (i: Int in 0..routineScheduleList.size-1) {
                var dates = ArrayList<CalendarDay>() // 점을 찍을 날짜들을 여기에 add로 담아주면 됨
                // 하나의 반복 일정에 대해
                for (j: Int in 0..routineScheduleList[i].dayList!!.size-1) {
                    var routine = routineScheduleList[i].dayList!![j] // 날짜
                    val routine_year = routine.get(Calendar.YEAR)
                    val routine_month = routine.get(Calendar.MONTH)
                    val routine_day = routine.get(Calendar.DAY_OF_MONTH)
                    var date = Calendar.getInstance()
                    date.set(routine_year, routine_month, routine_day)

                    var day = CalendarDay.from(date) // Calendar 자료형을 넣어주면 됨
                    dates.add(day)
                }
                routineDates.add(dates)
            }
        }

        // 사전작업3. 파이어베이스에서 가져와서 표시해주기
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
            calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), dates)) // 점 찍기
            for (v: Int in 0..routineDates.size-1) {
                calendar!!.addDecorator(EventDecorator(Color.parseColor("#3f51b5"), routineDates[v])) // 점 찍기
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
        // 달을 바꿨을 때 "yyyy년 yy월" 형태로 표기하기
        widget?.setTitleFormatter(TitleFormatter {
            val simpleDateFormat = SimpleDateFormat("yyyy년 MM월", Locale.KOREA)
            simpleDateFormat.format(date?.date?.time)
        })
        // 뷰페이저도 초기화 해주고 정보 다시 가져와야 한다
        var year = date?.year
        var month = date?.month
        var day = date?.day

        var calc = Calendar.getInstance()
        calc.set(year!!, month!!, day!!)

        var scheduleDatabase = CalendarDatabase.getInstance(this.context)
        var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(this.context)

        CoroutineScope(Dispatchers.IO).launch {
            // 내 아이디를 포함하고 있는 스터디 id 가져오기
            var viewPagerInfo = ArrayList<ArrayList<Schedule>>()
            launch {
                var study_id_list = getStudyIdList()
                var study_schedule_list = getStudyScheduleList(study_id_list)

                // 뷰 페이저에 넣을 내용들(한 달간의 일정들)
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
                CalendarPagerAdapter(viewPagerInfo, this@CalendarFragment) // 뷰 페이저 만들어주기


            binding.calendarViewpager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    calc.set(year, month, position + 1) // position은 0부터 시작, 날짜는 1부터 시작하므로
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
        // 지금 11월달이 10월로, 1월달이 0월로 표기된다. Month에 +1을 하고 보여줘야 함
        var selectedDay = date.day
        var selectedMonth = date.month + 1
        var selectedYear = date.year
        if (selected) {
            selectedDate(selectedDay) // 선택한 날짜로 이동

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
            builder.setTitle("안내")
            builder.setMessage("스터디 일정은 개인 캘린더에서 삭제할 수 없습니다.")
            builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
            })
        } else {
            builder.setTitle("일정")
            builder.setMessage("일정을 삭제하시겠습니까?")
            builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
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
                            Log.d("test", "확인")
                        }
                    }.join()
                    Log.d("test2", "확인2")
                    updateAdapter()
                }
            })
            builder.setNegativeButton("취소") {
                    _, _ -> println("취소 버튼")
            }
        }

        builder.show()
    }

    fun updateAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            // 내 아이디를 포함하고 있는 스터디 id 가져오기
            var study_id_list = getStudyIdList()
            var study_schedule_list = getStudyScheduleList(study_id_list)

            // 뷰페이저2 사용
            // 첫 화면에서 보여줄 달의 정보를 가지고 있는 뷰를 여기서 만들어줘야 한다.
            var calc = Calendar.getInstance()
            calc.set(currentYear, currentMonth, currentDay)

            var scheduleDatabase = CalendarDatabase.getInstance(context)
            var routineScheduleDatabase = RoutineScheduleDatabase.getInstance(context)
            // 처음 보여줄 날짜
            var firstYear = calc.get(Calendar.YEAR)
            var firstMonth = calc.get(Calendar.MONTH)
            var firstDay = calc.get(Calendar.DATE)

            // 뷰 페이저에 넣을 내용들(한 달간의 일정들)
            var viewPagerInfo = calculateCalendarDates(firstYear, firstMonth, firstDay, scheduleDatabase, routineScheduleDatabase, study_schedule_list)
            binding.calendarViewpager.adapter = CalendarPagerAdapter(viewPagerInfo, this@CalendarFragment) // 뷰 페이저 만들어주기
            dotDecorator(binding.calendarView, scheduleDatabase, routineScheduleDatabase, totalStudySchedule)

            selectedDate(currentDay) // 선택한 날짜로 이동
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