package com.ssafy.jobis.presentation.community.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.R
import com.ssafy.jobis.data.repository.CommunityRepository
import com.ssafy.jobis.databinding.CommunitySearchActivityBinding
import com.ssafy.jobis.presentation.community.detail.CommunityDetailActivity
import com.ssafy.jobis.presentation.community.search.ui.CommunitySearchFragment
import java.lang.IllegalArgumentException

class CommunitySearchActivity : AppCompatActivity() {

    private lateinit var binding: CommunitySearchActivityBinding
    private val communitySearchViewModel: CommunitySearchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CommunitySearchViewModel::class.java)) {
                    return CommunitySearchViewModel(
                        communityRepository = CommunityRepository()
                    ) as T
                }
                throw IllegalArgumentException("UnKnown ViewModel Class")
            }
        }
    }
    private val getResultCommunityDetail = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            var word: String? = binding.communitySearchView.query.toString()
            if (word != null) {
                communitySearchViewModel.searchPost(word!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommunitySearchActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        communitySearchViewModel.postList.observe(this,
            Observer { postList ->
                postList ?: return@Observer
                if (postList.size == 0) {
                    binding.serachTextView.visibility = View.VISIBLE
                } else {
                    binding.serachTextView.visibility = View.GONE
                }
            })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.communitySearchFrameLayout, CommunitySearchFragment.newInstance())
                .commitNow()
        }

        binding.communitySearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.d("test", "${p0}")
                if (p0 != null) {
                    communitySearchViewModel.searchPost(p0)
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    communitySearchViewModel.test(p0)
                }
                return false
            }
        })
        binding.communitySearchView.isIconified = false
    }

    fun goCommunityDetailActivity(post_id: String) {
        val intent = Intent(this, CommunityDetailActivity::class.java)
        intent.putExtra("id", post_id)
        getResultCommunityDetail.launch(intent)
    }

}