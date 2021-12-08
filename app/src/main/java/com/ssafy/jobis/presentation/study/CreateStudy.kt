package com.ssafy.jobis.presentation.study

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Crew
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.data.model.study.StudyDatabase
import com.ssafy.jobis.databinding.ActivityCreateStudyBinding
import com.ssafy.jobis.presentation.chat.ChatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CreateStudy : AppCompatActivity() {

    private var mBinding: ActivityCreateStudyBinding? = null
    private val binding get() = mBinding!!

    private var location:String? = "서울"
    private var topic: String? = "IT"
    private var max_user:Any? = 1

    private val mAuth = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCreateStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val region_list = resources.getStringArray(R.array.study_region)
        val region_adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,region_list)


        val topic_list = resources.getStringArray(R.array.study_topic)
        val topic_adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,topic_list)

        val population_list = resources.getStringArray(R.array.study_population)
        val population_adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,population_list)


        region_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.spRegion.adapter = region_adapter

        topic_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.spTopic.adapter = topic_adapter


        population_adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.spPopulation.adapter = population_adapter

        binding.spRegion.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                location = binding.spRegion.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


        binding.spTopic.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                topic = binding.spTopic.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.spPopulation.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                max_user = binding.spPopulation.getItemAtPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etDescribe.text.toString()

            val location = location
            val topic = topic
            val max_user = max_user.toString().toInt()
            val current_user = 1

            val username = Crew(mAuth!!.uid.toString())
            val user_list = mutableListOf(username)
            val curTime = Calendar.getInstance().time
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일 hh:mm a")
//            val sdf = SimpleDateFormat("hh:mm a")
            val time = sdf.format(curTime)




            RequestNewStudy(title, content, location, topic, max_user, current_user, user_list, time, 0)
            finish()
        }
    }

    private fun RequestNewStudy(
        title:String,
        content:String? = null,
        location:String? = null,
        topic:String?,
        max_user:Int = 1,
        current_user:Int = 1,
        user_list:MutableList<Crew>? = null,
        created_at:String,
        unread_chat_cnt:Int = 0) {


        val a = Study(
            title = title,
            content = content,
            location = location,
            topic = topic,
            max_user = max_user,
            current_user = current_user,
            user_list = user_list,
            created_at = created_at,
            unread_chat_cnt = unread_chat_cnt
        )

            Log.i("maxuser", a.toString())
            val ref = FirebaseDatabase.getInstance().getReference("/Study")

            var pushedStudy = ref.push()
            Log.d("스터디 업로드", "스터디 업로드")
            pushedStudy.setValue(a)
                .addOnCompleteListener { task ->
                    Log.d("스터디 업로드 완료", "스터디 업로드 완료")
                    if (task.isSuccessful) {


                        // 로컬에 만들어주고
                        CoroutineScope(Dispatchers.IO).launch {
                            a.id = pushedStudy.key.toString()
                            val db = StudyDatabase.getInstance(this@CreateStudy)
                            db!!.getStudyDao().insertStudy(a)

                            // firebase db에 key 업데이트 해줘야함
                            pushedStudy.setValue(a)

                            // 나중에 방 만들면 바로 넘어가는거 구현
                            var intent = Intent(applicationContext, ChatActivity::class.java)
                            intent.putExtra("study_id", a.id)
                            intent.putExtra("study_title", a.title)
                            startActivity(intent)
                        }



                    } else {
                        Toast.makeText(applicationContext, "Failed...", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Log.d("스터디 만들기 실패", "실패 실패 실패")
                }
        }


    }


