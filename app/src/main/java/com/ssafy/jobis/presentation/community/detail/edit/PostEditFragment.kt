package com.ssafy.jobis.presentation.community.detail.edit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.ssafy.jobis.R
import com.ssafy.jobis.data.model.community.Post
import com.ssafy.jobis.databinding.FragmentPostEditBinding
import com.ssafy.jobis.presentation.community.detail.CommunityDetailActivity
import com.ssafy.jobis.presentation.login.Jobis


class PostEditFragment : Fragment() {

    private var _binding: FragmentPostEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var category: String
    private lateinit var post_id: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        category = arguments?.getString("category")!!
        post_id = arguments?.getString("post_id")!!
        var content = arguments?.getString("content")
        var title = arguments?.getString("title")
        binding.categoryEditButton.text = category
        binding.editTextPostEditContent.setText(content)
        binding.editTextPostEditTitle.setText(title)
        binding.createPostEditButton.setOnClickListener {
            val currentUser = Firebase.auth.currentUser
            val communityDetailActivity = (activity as CommunityDetailActivity)
            if (currentUser != null) {
                // firebase 요청 보내기
                val content = binding.editTextPostEditContent.text.toString()
                val title = binding.editTextPostEditTitle.text.toString()
                val post = Post(
                    title=title,
                    content=content,
                    user_id=currentUser.uid,
                    user_nickname = Jobis.prefs.getString("nickname", "???"),
                    category = category
                )
                var db = FirebaseFirestore.getInstance()
                db.collection("posts").document(post_id)
                    .set(post)
                    .addOnSuccessListener {
                        val appContext = context?.applicationContext
                        Toast.makeText(appContext, "게시글이 수정되었습니다.", Toast.LENGTH_LONG).show()
                        communityDetailActivity.goPostDetailFragment(post_id)
                    }
                    .addOnFailureListener {
                        val appContext = context?.applicationContext
                        Toast.makeText(appContext, "게시글 수정에 실패했습니다.", Toast.LENGTH_LONG).show()
                    }
            } else {
                val appContext = context?.applicationContext
                Toast.makeText(appContext, "존재하지 않는 사용자입니다.", Toast.LENGTH_LONG).show()
            }
        }

        binding.categoryEditButton.setOnClickListener {
            showPopup()
        }

    }

    private fun showPopup() {
        val communityDetailActivity = (activity as CommunityDetailActivity)
        val inflater = communityDetailActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        val alertDialog = AlertDialog.Builder(communityDetailActivity)
            .setTitle("분야를 선택해주세요.")
            .setItems(mList, DialogInterface.OnClickListener { dialogInterface, i ->
                category = mList[i]
                binding.categoryEditButton.text = category
            })
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

}