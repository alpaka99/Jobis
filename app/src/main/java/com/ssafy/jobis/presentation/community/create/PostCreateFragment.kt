package com.ssafy.jobis.presentation.community.create

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ssafy.jobis.data.model.community.Post
import com.ssafy.jobis.databinding.PostCreateFragmentBinding
import com.ssafy.jobis.presentation.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.login.Jobis

class PostCreateFragment : Fragment() {

    companion object {
        fun newInstance() = PostCreateFragment()
    }
    private lateinit var mainAcitivty: MainActivity
    private var _binding: PostCreateFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var category: String
    private lateinit var uid: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PostCreateFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showPopup()
        binding.createPostButton.setOnClickListener {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                // firebase 요청 보내기
                val content = binding.editTextPostContent.text.toString()
                val title = binding.editTextPostTitle.text.toString()
                val post = Post(
                    title=title,
                    content=content,
                    user_id=currentUser.uid,
                    user_nickname = Jobis.prefs.getString("nickname", "???"),
                    category = category
                )
                var db = FirebaseFirestore.getInstance()
                var userRef = db.collection("users").document(currentUser.uid)
                db.collection("posts")
                    .add(post)
                    .addOnSuccessListener { documentReference ->
                        var post_id = documentReference.id
                        userRef.update("article_list", FieldValue.arrayUnion(post_id))
                        val appContext = context?.applicationContext
                        Toast.makeText(appContext, "게시글이 등록되었습니다.", Toast.LENGTH_LONG).show()
                        mainAcitivty?.goCommunityFragment()
                    }
                    .addOnFailureListener {
                        val appContext = context?.applicationContext
                        Toast.makeText(appContext, "게시글 등록에 실패했습니다.", Toast.LENGTH_LONG).show()
                    }
            } else {
                val appContext = context?.applicationContext
                Toast.makeText(appContext, "존재하지 않는 사용자입니다.", Toast.LENGTH_LONG).show()
            }
        }

        binding.categoryButton.setOnClickListener {
            showPopup()
        }

        binding.postCreateBackButton.setOnClickListener {
            mainAcitivty.supportFragmentManager.beginTransaction().remove(this)
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) mainAcitivty = context
    }

    private fun showPopup() {
        val inflater = mainAcitivty.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.select_popup, null)
        val mList = arrayOf(
            "경영/사무",
            "마케팅/광고",
            "유통/물류",
            "영업",
            "IT",
            "생산/제조",
            "연구개발/설계",
            "전문직",
            "디자인/미디어",
            "건설",
            "서비스",
            "기타")

        val alertDialog = AlertDialog.Builder(mainAcitivty)
            .setTitle("분야를 선택해주세요.")
            .setItems(mList, DialogInterface.OnClickListener { dialogInterface, i ->
                category = mList[i]
                binding.categoryButton.text = category
            })
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }
}