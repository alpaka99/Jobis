package com.ssafy.jobis.presentation.community.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import com.ssafy.jobis.R
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.databinding.CommunityDetailActivityBinding
import com.ssafy.jobis.presentation.community.detail.edit.PostEditFragment
import com.ssafy.jobis.presentation.community.detail.ui.detail.CommunityDetailFragment

class CommunityDetailActivity : AppCompatActivity() {

    private lateinit var binding: CommunityDetailActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommunityDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            val fragment = CommunityDetailFragment.newInstance()
            val id = intent.getStringExtra("id")
            fragment.arguments = bundleOf("id" to id)
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commitNow()
        }

        binding.postDetailBackButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    fun goPostEditFragment(post_id: String, uid: String, post: PostResponse) {
        val fragment = PostEditFragment()
        val bundle = Bundle()
        Log.d("test", "${post} ${post_id}")
        bundle.putString("post_id", post_id)
        bundle.putString("uid", uid)
        bundle.putString("content", post.content)
        bundle.putString("category", post.category)
        bundle.putString("title", post.title)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    fun goPostDetailFragment(post_id: String) {
        val fragment = CommunityDetailFragment()
        fragment.arguments = bundleOf("id" to post_id)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }

    fun loadingOn() {
        binding.communityDetailProgressBar.visibility = View.VISIBLE
    }
    fun loadingOff() {
        binding.communityDetailProgressBar.visibility = View.GONE
    }
}

