package com.ssafy.jobis.presentation.calendar

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.calendar.CalendarDatabase
import com.ssafy.jobis.data.model.calendar.RoutineSchedule
import com.ssafy.jobis.data.model.calendar.RoutineScheduleDatabase
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.databinding.FragmentRoutineScheduleBinding
import kotlinx.android.synthetic.main.fragment_routine_schedule.*
import kotlinx.android.synthetic.main.fragment_routine_schedule.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*


class RoutineScheduleFragment(val activity: Activity, private val year: Int, val month: Int, val day: Int) : Fragment() {
//    interface OnClickCreateScheduleListener {
//        fun onClickCreateSchedule(title: String, content: String)
//    }
    var title = ""
    var content = ""
    private lateinit var newRoutineSchedule : RoutineSchedule
    private var _binding: FragmentRoutineScheduleBinding? = null
    private val binding get() = _binding!!
    private var startDateString =""
    private var startTimeString = ""
    private var endDateString =""
    private var endTimeString = ""
    private var startHour = 0
    private var startMinute = 0
    private var startYear = 0
    private var startMonth = 0
    private var startDay = 0
    private var endYear = 0
    private var endMonth = 0
    private var endDay = 0
    private var endHour = 0
    private var endMinute = 0
    var scheduleId : Long = 0
    private val dayOfWeekSelect = arrayOf(false, false, false, false, false, false, false, false)

    @SuppressLint("SimpleDateFormat")
    private val dateFormat1 = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val dateFormat2 = SimpleDateFormat("HH:mm")
    var calendar: Calendar = Calendar.getInstance() // 현재 날짜 및 시간 저장
    private var scheduleStartDate = ""
    private var scheduleStartTime = ""
    private var scheduleEndDate = "" // 이거 없어도 되겠는데?
    private var scheduleEndTime = ""

    private fun setAlarm(RoutineSchedule: RoutineSchedule, scheduleId: Long, title:String, content:String) {

        // 일정 객체에서 시작 시간 받아와서 나누기
        val time = RoutineSchedule.startTime.split(":")
        val hourOfDay = time[0].toInt()
        val minute = time[1].toInt()

        val dayList = RoutineSchedule.dayList
        val dayListSize = dayList?.size
        println("데이리스트")
        println(dayList)
        for (i in 0 until dayListSize!!) { // 0..4 면 0~4까지 돌지만, 0 until 4면 0~3까지 돈다! python for문처럼
            println("데이리스트사이즈만큼순회, $i")
            println(dayList[i])
            println(dayList[i].timeZone)
            val year = dayList[i].get(Calendar.YEAR)
            val month = dayList[i].get(Calendar.MONTH)
            val day = dayList[i].get(Calendar.DAY_OF_MONTH)
            val alarmCalendar = Calendar.getInstance()
            alarmCalendar.set(Calendar.YEAR, year)
            alarmCalendar.set(Calendar.MONTH, month)
            alarmCalendar.set(Calendar.DAY_OF_MONTH, day)
            alarmCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            alarmCalendar.set(Calendar.MINUTE, minute)
            alarmCalendar.set(Calendar.SECOND, 0)
            println("스케쥴아이디, $scheduleId")
//            var scheduleIdToInt = scheduleId.toInt()
//            println("스케쥴아이디 int, $scheduleIdToInt")
            val scheduleIdToInt = scheduleId.toInt()
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this.context, AlarmReceiver::class.java)  // 1
            intent.putExtra("title", "$title")
            intent.putExtra("content", "$content")

            var pendingIntent = PendingIntent.getBroadcast(this.context, scheduleIdToInt, intent, PendingIntent.FLAG_CANCEL_CURRENT)

            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.timeInMillis, pendingIntent)
            }
            else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.timeInMillis, pendingIntent)
            }
            println("알람정보, $i, 번째, $year, $month, $day, $hourOfDay, $minute, $scheduleIdToInt")
            println("--------알람설정됨-----,$i")
//        Toast.makeText(this.context, "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show()
        }



    }


    @SuppressLint("SimpleDateFormat")
    fun routinScheduleAddFun():Int{
        // 시작 날짜 따로 안정했으면
        if (startYear == 0) {
            // 시작 날짜가 0 할당이므로 포맷 바꿔서 변수에 저장. 그냥 calendar 써도 됨
            startYear = year
            startMonth = month - 1
            startDay = day
            println("어디부터 1, $startYear, $startMonth, $startDay")
        }
        // 시작 시간 안정했으면
        if (startTimeString == "") {
            // 시작 시간 "" 이므로 그냥 현재 시간 할당한거 저장
            startTimeString = scheduleStartTime
        }
        // 끝 날짜 안정했으면
        if (endDateString==""){
            endYear = year
            endMonth = month -1
            endDay = day
        }
        // 끝 시간 안정했을경우
        if (endTimeString == "") {
            endTimeString = scheduleEndTime // 현재시간 + 10분 시간을 저장
        }


        // 일정 제목 내용 받아오기
        title = scheduleTitleEditText.text.toString()
        content = scheduleContentEditText.text.toString()

        if (title==""){
            Toast.makeText(context,R.string.empty_title, Toast.LENGTH_SHORT).show()
            return 0

        }
        if (content==""){
            Toast.makeText(context,R.string.empty_content, Toast.LENGTH_SHORT).show()
            return 0
        }

        if (endHour<startHour){
            Toast.makeText(context,R.string.time_select_error, Toast.LENGTH_SHORT).show()
            return 0
        } else if (endHour == startHour && endMinute < startMinute) {
            Toast.makeText(context,R.string.time_select_error, Toast.LENGTH_SHORT).show()
            return 0
        }

        // Calendar 객체에 시작 날짜 넣어서 저장
        val startCal = Calendar.getInstance()
        startCal.set(startYear, startMonth, startDay)
        println("어디부터2, $startYear, $startMonth, $startDay")
        // Calendar 객체에 끝 날짜 넣어서 저장
        val endCal = Calendar.getInstance()
        endCal.set(endYear, endMonth, endDay)

        // 시작하는 날짜의 날 (int, nnn) (1~365로 나타난다)
        val startDayOfYear = startCal.get(Calendar.DAY_OF_YEAR)
        // 끝 날짜
        val endDayOfYear = endCal.get(Calendar.DAY_OF_YEAR)

        if (startDay==endDay && startMonth==endMonth && startYear==endYear ){
            Toast.makeText(context,R.string.day_select_error2, Toast.LENGTH_SHORT).show()
            return 0
        }
        if(startYear > endYear) {
            println("$startDayOfYear, $endDayOfYear, 시작일이 종료일보다 늦습니다. ")
            Toast.makeText(context,R.string.day_select_error1, Toast.LENGTH_SHORT).show()
            return 0
        } else if (startYear == endYear && startMonth > endMonth) {
            println("$startDayOfYear, $endDayOfYear, 시작일이 종료일보다 늦습니다. ")
            Toast.makeText(context,R.string.day_select_error1, Toast.LENGTH_SHORT).show()
            return 0
        } else if (startYear == endYear && startMonth == endMonth && startDay > endDay) {
            println("$startDayOfYear, $endDayOfYear, 시작일이 종료일보다 늦습니다. ")
            Toast.makeText(context,R.string.day_select_error1, Toast.LENGTH_SHORT).show()
            return 0
        }


        // 시작하는 날짜의 요일
        val startDayOfWeek= startCal.get(Calendar.DAY_OF_WEEK)

        val endMilli = endCal.timeInMillis
//            println("끝밀리, $endMilli")
        val endMilliToDate = endMilli / (24*60*60*1000)
        println("끝밀리투데이트,$endMilliToDate")
        // 버튼 클릭할때마다 새로 담아야해서 안쪽에서 선언
        // 1~7까지 순회하면서
        val routineDaySelect = mutableListOf<Calendar>()
        println("______________________________________________________")
        println(startDayOfWeek)
        for(i in 1..7) {
            if (dayOfWeekSelect[i]) { // 요일이 true 일 경우
                when {
                    i == startDayOfWeek -> { // 시작 날짜의 요일 == 반복 선택한 요일 중 지금 보고있는 거
                        val cal0 = Calendar.getInstance()
                        cal0.set(startYear, startMonth, startDay)
                        val cal1MilliToDate = cal0.timeInMillis / (24*60*60*1000)
                        println("cal1mtd, $cal1MilliToDate")
                        val diff = (endMilliToDate - cal1MilliToDate)/7
                        var tmp = 0
                        while(diff >= tmp){ //
                            val cal1 = Calendar.getInstance()
                            cal1.set(startYear, startMonth, startDay)
                            cal1.add(Calendar.DATE, 7 * tmp)
                            routineDaySelect.add(cal1)
                            println("cal1: $cal1")
                            println(cal1.get(Calendar.DAY_OF_YEAR))
                            tmp += 1
                        }
                    }

                    i > startDayOfWeek -> {// 시작 날짜의 요일 < 반복 선택한 요일 중 지금 보고있는 거
                        // 며칠 차이인지 확인해서 + a
                        // 7일 더하면서 반복 저장
                        val cal0 = Calendar.getInstance()
                        cal0.set(startYear, startMonth, startDay)
                        val add = i - startDayOfWeek
                        cal0.add(Calendar.DATE, add)
                        val cal2MilliToDate = cal0.timeInMillis / (24*60*60*1000)
                        println("cal2mtd, $cal2MilliToDate")
                        val diff = (endMilliToDate - cal2MilliToDate)/7
                        var tmp = 0
                        while(diff >= tmp){ //
                            val cal2 = Calendar.getInstance()
                            cal2.set(startYear, startMonth, startDay)
                            cal2.add(Calendar.DATE, add) // 요일 맞추기
                            cal2.add(Calendar.DATE, 7 * tmp)
                            routineDaySelect.add(cal2)

                            tmp += 1
                        }
                    }
                    else -> { // 시작 날짜의 요일 > 반복 선택한 요일 중 지금 보고있는 거
                        // 며칠 차이인지 확인해서 + a
                        // 7일 더하면서 반복 저장
                        val add = 7 - startDayOfWeek + i
                        val cal0 = Calendar.getInstance()
                        cal0.set(startYear, startMonth, startDay)
                        cal0.add(Calendar.DATE, add)
                        val cal3MilliToDate = cal0.timeInMillis / (24*60*60*1000)
                        println("cal3mtd, $cal3MilliToDate")
                        val diff = (endMilliToDate - cal3MilliToDate)/7
                        var tmp = 0
                        while(diff >= tmp){ //
                            val cal3 = Calendar.getInstance()
                            cal3.set(startYear, startMonth, startDay)
                            cal3.add(Calendar.DATE, add) // 요일 맞추기
                            cal3.add(Calendar.DATE, 7 * tmp)
                            routineDaySelect.add(cal3)

                            tmp += 1
                        }
                    }
                }
            }
        }
        if (routineDaySelect.size == 0) {
            Toast.makeText(context,R.string.empty_routine_select, Toast.LENGTH_SHORT).show()
            return 0
        }
        println("------선택된 날----")
        println(routineDaySelect)
        val size1 = routineDaySelect.size
        println("사이즈,$size1")
        routineDaySelect.sort()
        // 시작 날짜 , 끝 날짜, 요일, 시간, 제목, 내용 다 제대로 들어왔을 경우에만 초기화 되긴 해
        // 아닌 경우엔 중간에 return으로 빠져나감
        // db 저장
        newRoutineSchedule = RoutineSchedule(title, content,routineDaySelect, startTimeString, endTimeString, 0, "", "")
        val db = RoutineScheduleDatabase.getInstance(activity)
        CoroutineScope(Dispatchers.IO).launch {
            scheduleId = db!!.routineScheduleDao().insert(newRoutineSchedule)
            val dbList = db.routineScheduleDao().getAll()
            println("DB 결과: $dbList")
        }
        setAlarm(newRoutineSchedule, scheduleId, title, content)
        return 1
    }


    // 이건 activity에서 못쓰나?


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineScheduleBinding.inflate(inflater, container, false)
        val view = binding.root


        // 버튼 클릭시 해당 요일의 boolean 값이 전환됨 (기본 false), 이미지 변경
        view.dayOfWeek1.setOnClickListener {
            dayOfWeek1.isSelected = !dayOfWeek1.isSelected
            dayOfWeekSelect[1] = !dayOfWeekSelect[1]
        }
        view.dayOfWeek2.setOnClickListener {
            dayOfWeek2.isSelected = !dayOfWeek2.isSelected
            dayOfWeekSelect[2] = !dayOfWeekSelect[2]}
        view.dayOfWeek3.setOnClickListener {
            dayOfWeek3.isSelected = !dayOfWeek3.isSelected
            dayOfWeekSelect[3] = !dayOfWeekSelect[3]}
        view.dayOfWeek4.setOnClickListener {
            dayOfWeek4.isSelected = !dayOfWeek4.isSelected
            dayOfWeekSelect[4] = !dayOfWeekSelect[4]}
        view.dayOfWeek5.setOnClickListener {
            dayOfWeek5.isSelected = !dayOfWeek5.isSelected
            dayOfWeekSelect[5] = !dayOfWeekSelect[5]}
        view.dayOfWeek6.setOnClickListener {
            dayOfWeek6.isSelected = !dayOfWeek6.isSelected
            dayOfWeekSelect[6] = !dayOfWeekSelect[6]}
        view.dayOfWeek7.setOnClickListener {
            dayOfWeek7.isSelected = !dayOfWeek7.isSelected
            dayOfWeekSelect[7] = !dayOfWeekSelect[7]
        }

//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//
//            }
//        }
        calendar = Calendar.getInstance()
//        calendar.add(Calendar.HOUR, 9)
        val currentCal = calendar
//        var currentCal = calendar // 10분 추가한 cal 객체랑 따로 저장하고 싶어서 분리한건데 잘 안되는듯?
//        scheduleStartDate = dateFormat1.format(currentCal.time)
        scheduleStartDate = if (day<=9 && month <= 8) {  // 월 일 전부 한자리
            "${year}-0${month}-0${day}"
        } else if (month <= 9) { // 월만 한자리
            "${year}-0${month}-${day}"
        } else if (day <= 9 ){ // 일만 한자리
            "${year}-${month}-0${day}"
        } else { // 처리할 필요 없음
            "${year}-${month}-${day}"
        }
//        scheduleStartDate = "$year-$month-$day"
        scheduleStartTime = dateFormat2.format(currentCal.time)
        startHour = currentCal.get(Calendar.HOUR_OF_DAY)
        startMinute = currentCal.get(Calendar.MINUTE)
        scheduleEndDate = dateFormat1.format(calendar.time) // 이거 없어도 되겠는데?

        // 현재 시간 50~59분 이면 59로 강제 지정 -> 안그러면 날짜 변경됨
        if (SimpleDateFormat("HH").format(calendar.time) == "23" ){
            if (SimpleDateFormat("mm").format(calendar.time).toInt() in 50..59){

                calendar.set(Calendar.MINUTE, 59)
                scheduleEndTime = "23:59"
                endMinute = 59
                endHour = calendar.get(Calendar.HOUR_OF_DAY)
            }
            else{
                calendar.add(Calendar.MINUTE, 10) // 10분 후 자동 지정
                scheduleEndTime = dateFormat2.format(calendar.time)
                endHour = calendar.get(Calendar.HOUR_OF_DAY)
                endMinute = calendar.get(Calendar.MINUTE)
            }
        } else{
            calendar.add(Calendar.MINUTE, 10) // 10분 후 자동 지정
            scheduleEndTime = dateFormat2.format(calendar.time)
            endHour = calendar.get(Calendar.HOUR_OF_DAY)
            endMinute = calendar.get(Calendar.MINUTE)
        }


        // 화면 로딩시 보이는 시간! +10분 후가 기본. 23:50~59 일때는 23:59로 픽스
        view.startDateBtn.text = scheduleStartDate
        view.startTimeBtn.text = scheduleStartTime
        view.endDateBtn.text = scheduleStartDate
        view.endTimeBtn.text = scheduleEndTime

        // 시작일 선택 버튼
        view.startDateBtn.setOnClickListener {
            // OnDateSetListener : 사용자가 날짜 선택을 완료했음을 알림, 내부 기능 작성
            // view 대신 _ 쓰래서 바꾸긴 했는데 무슨 차인지 모름
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                // 1~9일 선택하더라도 포맷 맞추려고 설정
                startDateString = if (dayOfMonth<=9 && month <= 8) {  // 월 일 전부 한자리
                    "${year}-0${month+1}-0${dayOfMonth}"
                } else if (month <= 9) { // 월만 한자리
                    "${year}-0${month+1}-${dayOfMonth}"
                } else if (dayOfMonth <= 9 ){ // 일만 한자리
                    "${year}-${month+1}-0${dayOfMonth}"
                } else { // 처리할 필요 없음
                    "${year}-${month + 1}-${dayOfMonth}"
                }
                // 시작 날짜를 선택했을 때 0에서 고른 날짜로 변경됨
                startYear = year
                startMonth = month
                startDay = dayOfMonth
                // 선택한 날짜로 글자 변경
                startDateBtn.text = startDateString
            }

            // DatePicker Dialog 창 띄움
            if (startDateString == "") { // 초기 (시작 날짜 안정했을 경우)
                DatePickerDialog(
                    // 10분 추가하기 전의 cal 객채에서 년 월 일을 기본으로 설정
                    activity, dateSetListener, year,
                    month - 1, day,
                ).show()
            } else { // 선택 후 다시 열 경우, 선택했던 날짜를 기본으로 설정해서 연다
                DatePickerDialog(activity, dateSetListener, startYear, startMonth, startDay).show()
            }

        }
        // 시작시간 선택 버튼
        view.startTimeBtn.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                startTimeString = if (hourOfDay <= 9) { // 시간 포맷 맞춰서 추가
                    "0${hourOfDay}:"
                } else {
                    "${hourOfDay}:"
                }
                startTimeString += if (minute <= 9) { // 분 포맷 맞춰서 추가
                    val s = "0${minute}"
                    s // 마지막 줄만 리턴됨
                } else {
                    "$minute"
                }
                // 재선택 할 때 쓰려고 변수에 저장
                startHour = hourOfDay
                startMinute = minute
                // 버튼에 선택한 시간 보이게 하기
                startTimeBtn.text = startTimeString
            }

            TimePickerDialog(
                activity,
                timeSetListener,
                startHour,
                startMinute,
                true,
            ).show()
        }
        // 끝날짜 선택 버튼
        view.endDateBtn.setOnClickListener {
            // OnDateSetListener : 사용자가 날짜 선택을 완료했음을 알림, 내부 기능 작성
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                endDateString = if (dayOfMonth<=9 && month <= 8) { // 1~9일 선택하더라도 포맷 맞추려고 설정
                    "${year}-0${month+1}-0${dayOfMonth}"
                } else if (month <= 8) {
                    "${year}-0${month+1}-${dayOfMonth}"
                } else if (dayOfMonth <= 9 ){
                    "${year}-${month+1}-0${dayOfMonth}"
                } else {
                    "${year}-${month + 1}-${dayOfMonth}"
                }
                endYear = year
                endMonth = month
                endDay = dayOfMonth
                endDateBtn.text = endDateString
            }
            // Dialog 창 띄움
            if (endDateString == "") {
                DatePickerDialog(
                    // calendar = 10분 더한 객체, 50~59분일 경우 59로 맞춘 객체
                    activity, dateSetListener, year, month - 1, day,
                ).show()
            } else {
                DatePickerDialog(activity, dateSetListener, endYear, endMonth, endDay).show()
            }
        }
        // 끝 시간 설정 버튼
        view.endTimeBtn.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                endTimeString = if (hourOfDay <= 9) {
                    "0${hourOfDay}:"
                } else {
                    "${hourOfDay}:"
                }
                endTimeString += if (minute <= 9) {
                    "0${minute}"
                } else {
                    "$minute"
                }
                endHour = hourOfDay
                endMinute = minute
                endTimeBtn.text = endTimeString
            }
            if (endTimeString == "") {
                endHour = calendar.get(Calendar.HOUR_OF_DAY)
                endMinute = calendar.get(Calendar.MINUTE)
            }

            TimePickerDialog(
                activity,
                timeSetListener,
                endHour,
                endMinute,
                true,
            ).show()
        }


//        view.routineScheduleList.setOnClickListener {
//            println("일정 확인")
//            val db = RoutineScheduleDatabase.getInstance(activity)
//            CoroutineScope(Dispatchers.IO).launch {
//                val dbList = db!!.routineScheduleDao().getAll()
//                println("일정 결과: $dbList")
//            }
////            Log.d("로그 저장 정보", "$title, $content, $startYear, ${startMonth}, $startDay, $startTimeString, $endTimeString")
////            Log.d("시작끝시간", "$startHour, $endHour, $startMinute, $endMinute")
//        }


        return view





    }
}
//  프래그먼트 유지  삭제 등 onAttach, onDetach 메서드를 오버라이딩