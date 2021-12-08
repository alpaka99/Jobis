package com.ssafy.jobis.presentation.chat

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.ssafy.jobis.R
import com.ssafy.jobis.databinding.ActivityChatBinding
import com.ssafy.jobis.databinding.ActivityChatScheduleAddBinding
import java.util.*
import com.google.firebase.database.DataSnapshot
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ssafy.jobis.data.model.calendar.Schedule
import com.ssafy.jobis.presentation.calendar.AlarmReceiver
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList


class ChatScheduleAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatScheduleAddBinding
    var alarmReceiverInChat = AlarmReceiver()
    var dateString = ""
    var timeString = ""

//    private fun setAlarm(scheduleId: String, title:String, content:String, year: Int, month:Int, day:Int, startHour: Int, startDay: Int) {
//        val alarmCalendar = Calendar.getInstance()
//        alarmCalendar.set(Calendar.YEAR, year)
//        alarmCalendar.set(Calendar.MONTH, month)
//        alarmCalendar.set(Calendar.DAY_OF_MONTH, day)
//        alarmCalendar.set(Calendar.HOUR_OF_DAY, startHour)
//        alarmCalendar.set(Calendar.MINUTE, startDay)
//        alarmCalendar.set(Calendar.SECOND, 0)
//        println("스케줄 확인합니다, $year, $month, $day, $startHour, $startDay")
//        println("스케쥴 아이디, $scheduleId")
//        var scheduleIdToInt = scheduleId.toInt()

//        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val intent = Intent(this, alarmReceiverInChat::class.java)  // 1
//        intent.putExtra("title", title)
//        intent.putExtra("content", content)
//
//        var pendingIntent = PendingIntent.getBroadcast(this, , intent, PendingIntent.FLAG_CANCEL_CURRENT )
//
//        if (Build.VERSION.SDK_INT >= 23) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.timeInMillis, pendingIntent)
//        }
//        else {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.timeInMillis, pendingIntent)
//        }
//        Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show()
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatScheduleAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var currentStudyId = intent.getStringExtra("study_id").toString()
        println("current: " + currentStudyId)


        var study_info = FirebaseDatabase.getInstance().getReference("/Study").child(currentStudyId)
        var user_list = ArrayList<Map<String, String>>()
        study_info.child("user_list").get().addOnSuccessListener {
            user_list = it.value as ArrayList<Map<String, String>> // [{id=OStYPE}, {id=FKMN3MFS}, {id=SDLW32FZ}, ...]
            println("성공")
        }.addOnFailureListener {
            println("실패")
        }

        // 기본 시간 설정
        var currentTime = Calendar.getInstance()
        var currentYear = currentTime.get(Calendar.YEAR)
        var currentMonth = currentTime.get(Calendar.MONTH)
        var currentDay = currentTime.get(Calendar.DAY_OF_MONTH)
        var currentDayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK)
        var currentDayOfWeekString = dayOfWeekToString(currentDayOfWeek)
        var currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        var currentMinute = currentTime.get(Calendar.MINUTE)

        var currentDateString = "${currentYear}. ${currentMonth+1}. ${currentDay} ${currentDayOfWeekString}"
        var currentStartTimeString = "${currentHour}:${currentMinute}"
        var currentEndTimeString = ""

        if (currentMinute < 10) {
            currentStartTimeString = "${currentHour}:0${currentMinute}"
            currentEndTimeString = "${currentHour}:${currentMinute + 30}"
            if (currentHour < 10) {
                currentStartTimeString = "0${currentHour}:0${currentMinute}"
                currentEndTimeString = "0${currentHour}:${currentMinute + 30}"
            }
        }
        else if (currentMinute + 30 > 59) {
            currentStartTimeString = "${currentHour}:${currentMinute}"
            currentEndTimeString = "${currentHour}:${59}"
            if (currentHour < 10) {
                currentStartTimeString = "0${currentHour}:${currentMinute}"
                currentEndTimeString = "0${currentHour}:${59}"
            }
        } else {
            currentStartTimeString = "${currentHour}:${currentMinute}"
            currentEndTimeString = "${currentHour}:${currentMinute + 30}"
            if (currentHour < 10) {
                currentStartTimeString = "0${currentHour}:${currentMinute}"
                currentEndTimeString = "0${currentHour}:${currentMinute + 30}"
            }
        }


        binding.dateButton.text = currentDateString
        binding.startTimeButton.text = currentStartTimeString
        binding.endTimeButton.text = currentEndTimeString

        // 기본값
        var year = currentYear
        var month = currentMonth
        var day = currentDay
        var startHour = currentHour
        var startMinute = currentMinute
        var endHour = currentHour
        var endMinute = currentMinute + 30
        // 버튼 클릭시 datepicker 동작
        binding.dateButton.setOnClickListener {
            var cal = Calendar.getInstance()
            var temp_cal = Calendar.getInstance()
            var dateSetListener = DatePickerDialog.OnDateSetListener {
                view, yearVal, monthVal, dayOfMonthVal -> temp_cal.set(yearVal, monthVal, dayOfMonthVal)
                var dayOfWeek = temp_cal.get(Calendar.DAY_OF_WEEK)
                dateString = "${yearVal}. ${monthVal+1}. ${dayOfMonthVal} ${dayOfWeekToString(dayOfWeek)}"
                binding.dateButton.text = dateString
                // 지정한 값 넣어주기
                year = yearVal
                month = monthVal
                day = dayOfMonthVal
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()

        }
        // 버튼 클릭시 timepicker 동작
        binding.startTimeButton.setOnClickListener {
            var cal = Calendar.getInstance()
            var timeSetListener = TimePickerDialog.OnTimeSetListener { view, startHourOfDayVal, startMinuteVal ->
                timeString = "${startHourOfDayVal}:${startMinuteVal}"
                if (startHourOfDayVal < 10) {
                    timeString = "0${startHourOfDayVal}:${startMinuteVal}"
                    if (startMinuteVal < 10) {
                        timeString = "0${startHourOfDayVal}:0${startMinuteVal}"
                    }
                } else if (startMinuteVal < 10) {
                    timeString = "${startHourOfDayVal}:0${startMinuteVal}"
                }
                binding.startTimeButton.text = timeString
                startHour = startHourOfDayVal
                startMinute = startMinuteVal
            }
            var TimeDialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
            // spinner의 백그라운드 투명하게
            TimeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            TimeDialog.show()
        }
        // 버튼 클릭시 timepicker 동작
        binding.endTimeButton.setOnClickListener {
            var cal = Calendar.getInstance()
            var timeSetListener = TimePickerDialog.OnTimeSetListener { view, endHourOfDayVal, endMinuteVal ->
                timeString = "${endHourOfDayVal}:${endMinuteVal}"
                if (endHourOfDayVal < 10) {
                    timeString = "0${endHourOfDayVal}:${endMinuteVal}"
                    if (endMinuteVal < 10) {
                        timeString = "0${endHourOfDayVal}:0${endMinuteVal}"
                    }
                } else if (endMinuteVal < 10) {
                    timeString = "${endHourOfDayVal}:0${endMinuteVal}"
                }
                binding.endTimeButton.text = timeString
                endHour = endHourOfDayVal
                endMinute = endMinuteVal
            }
            var TimeDialog = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)+30, true)
            // spinner의 백그라운드 투명하게
            TimeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            TimeDialog.show()
        }


        // 이제 여기서 정보를 다 종합해서 schedule 객체를 채운 후 파이어베이스와 room을 통해 저장
        // 버튼에 클릭리스너를 달아서 editText 정보, 유저 정보, 저장할 정보 모두 사용
        binding.createScheduleButton.setOnClickListener {
            val title = binding.chatScheduleTitle.text.toString()
            val content = binding.chatScheduleContent.text.toString()
            var db = FirebaseFirestore.getInstance()

            if (endMinute > 59){
                endMinute = 59
            }
            var scheduleId = ""
            var newSchedule = Schedule(title, content, year, month, day, "${startHour}:${startMinute}", "${endHour}:${endMinute}", currentStudyId, 0, "")
            db.collection("study_schedules")
                .add(newSchedule)
                .addOnSuccessListener {
                    println("success!" + it.id)
                    scheduleId = it.id
                    returnChatScheduleActivity()
                }
                .addOnFailureListener { exception ->
                    Log.d("test", "${exception}")
                }

//            setAlarm(scheduleId, title,content, year, month, day, startHour, startMinute)

        }
    }
   fun dayOfWeekToString(dayOfWeek: Int): String {
        var dayOfWeekString = ""
        when (dayOfWeek) {
            1 -> dayOfWeekString = "일요일"
            2 -> dayOfWeekString = "월요일"
            3 -> dayOfWeekString = "화요일"
            4 -> dayOfWeekString = "수요일"
            5 -> dayOfWeekString = "목요일"
            6 -> dayOfWeekString = "금요일"
            7 -> dayOfWeekString = "토요일"
        }
        return dayOfWeekString
    }

    fun returnChatScheduleActivity() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    fun putZeroForward(s: String) : String {
        return "0" + s
    }
}