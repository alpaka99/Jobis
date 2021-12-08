package com.ssafy.jobis.presentation.study.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.study.Study
import com.ssafy.jobis.data.model.study.StudyDatabase
import com.ssafy.jobis.presentation.chat.ChatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ssafy.jobis.data.model.study.Crew


class SearchResultAdapter(val context: Context, val searchResultList: ArrayList<Study>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        return CustomViewHolder(view)
            .apply{
            itemView.setOnClickListener{
                CoroutineScope(Dispatchers.IO).launch {
                    val curPos:Int = adapterPosition

                    val tmpStudy = searchResultList!![curPos]

                    
                    // 방 인원이 넘어가면 못들어감
                    if (tmpStudy.current_user!! < tmpStudy.max_user!! ) {
                        val ref = FirebaseDatabase.getInstance().getReference("/Study")
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid

                        // 이미 내가 들어가있는 방인지 확인
                        val tmpCrew = Crew(uid)
                        if (tmpStudy.user_list!!.any{it == tmpCrew}) {
                            Handler(Looper.getMainLooper()).post{
                                Toast.makeText(context, "Already in this room", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            tmpStudy.user_list!!.add(Crew(uid))
                            tmpStudy.current_user = tmpStudy.current_user!! + 1

                            val db = StudyDatabase.getInstance(parent.context)
                            db!!.getStudyDao().insertStudy(searchResultList!![curPos])


                            val room_key = tmpStudy.id

                            val studyValues = tmpStudy.toMap()

                            val studyUpdates = hashMapOf<String, Any>(
                                "/$room_key" to studyValues
                            )

                            ref.updateChildren(studyUpdates)

                            var intent = Intent(context, ChatActivity::class.java)
                            intent.putExtra("isFirstTime", true)
                            intent.putExtra("study_id", searchResultList[curPos].id)
                            intent.putExtra("study_title", searchResultList[curPos].title)
                            context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                    else {
                        Handler(Looper.getMainLooper()).post{
                            Toast.makeText(context, "Room is FULL!!", Toast.LENGTH_SHORT).show()
                        }

                    }

                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is CustomViewHolder -> {
                if (searchResultList != null) {
                holder.bind(searchResultList[position])
            }}
        }

    }

    override fun getItemCount(): Int {
        return searchResultList?.size?:0
    }

    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {

            }
        }

        val title = itemView.findViewById<TextView>(R.id.tv_title)   // 스터디 이름
        val describe = itemView.findViewById<TextView>(R.id.tv_describe)  // 스터디 설명
        val startday = itemView.findViewById<TextView>(R.id.tv_startday_value)  // 스터디 시작일
        val population = itemView.findViewById<TextView>(R.id.tv_population_value)  // 스터디 인원

        @SuppressLint("SetTextI18n")
        fun bind(study: Study) {
            var startDateInfo = study.created_at
            var startYear = startDateInfo.substring(0,4)
            var startMonth =startDateInfo.substring(6,8)
            var startDay = startDateInfo.substring(10,12)
            var startDayChangeFormat = "$startYear.$startMonth.$startDay"

            title.text = study.title
            describe.text = study.content
            startday.text = startDayChangeFormat
            population.text = study.current_user.toString() + "/" + study.max_user.toString()
        }

    }

    private fun updateUserList() {

    }
}