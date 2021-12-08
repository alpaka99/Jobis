package com.ssafy.jobis.presentation.calendar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.MainActivity
import kotlinx.android.synthetic.main.activity_schedule.*


class CalendarScheduleActivity : AppCompatActivity() {
    lateinit var singleFragment: SingleScheduleFragment
    lateinit var routineFragment: RoutineScheduleFragment



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        var year = intent.getIntExtra("selected_year", 0)
        var month = intent.getIntExtra("selected_month", 0)
        var day = intent.getIntExtra("selected_day", 0)
        println("액티비티 데이터 전달: $year$month$day")

        singleFragment = SingleScheduleFragment(this, year, month, day)
        routineFragment = RoutineScheduleFragment(this, year, month, day)



        val spinner: Spinner = scheduleSpinner
        ArrayAdapter.createFromResource(this,
            R.array.routine,
            android.R.layout.simple_spinner_item).also{adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0){
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.scheduleFrameLayout, singleFragment)
                        .commit()
                } else if (position == 1) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.scheduleFrameLayout, routineFragment)
                        .commit()
                }
            }
        }

        scheduleTopAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.schedule_item1 -> {
                    val spinnerSelected = spinner.selectedItem
                    Log.d("스피너 아이템" , "$spinnerSelected")
                    when (spinnerSelected) {
                        "단일 일정" -> {
                            // db 저장
                            println("단일 일정 저장")
                            val returnInt = singleFragment.singleScheduleAddFun()
                            if (returnInt == 1) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        "반복 일정" -> {
                            // db 저장
                            println("반복 일정 저장")
                            val returnInt = routineFragment.routinScheduleAddFun()
                            if (returnInt == 1) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        else -> {
                            Log.d("스피너 선택", "스피너 선택 안됨")
                        }
                    }
                    true
                }
                else -> false
            }
        }

        scheduleTopAppBar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

//    override fun onClickCreateSchedule(title: String, content: String) {
//        Log.d("전달받은 데이터", "$title, $content")
//    }
}


