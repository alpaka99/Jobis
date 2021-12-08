package com.ssafy.jobis.presentation.study

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ssafy.jobis.data.model.study.Study

class SearchViewModel(application: Application): AndroidViewModel(application) {

    var searchList = arrayListOf<Study>()
    init {
        fetchStudy()
    }

    private fun fetchStudy() {

        var study: Study? = null

        val ref = FirebaseDatabase.getInstance().getReference("/Study")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.i("firebase", snapshot.getValue().toString())
                for (info in snapshot.children) {

                    // 와 이게되네...
                    val study = info.getValue(Study::class.java)
//                    Log.i("plz", study.toString())
                    searchList!!.add(study!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}