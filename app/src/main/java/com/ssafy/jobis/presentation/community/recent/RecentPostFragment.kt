package com.ssafy.jobis.presentation.community.recent

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssafy.jobis.R
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.data.response.PostResponseList
import com.ssafy.jobis.databinding.FragmentRecentPostBinding
import com.ssafy.jobis.presentation.MainActivity
import com.ssafy.jobis.presentation.community.CustomPostAdapter

class RecentPostFragment : Fragment() {

    private lateinit var recentPostViewModel: RecentPostViewModel
    private var _binding: FragmentRecentPostBinding ? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentPostViewModel = ViewModelProvider(this, RecentPostViewModelFactory())
            .get(RecentPostViewModel::class.java)
        recentPostViewModel.filteredPostList.observe(viewLifecycleOwner,
            Observer { filteredPostList ->
                Log.d("test", "sdfdddd ${filteredPostList}")
                filteredPostList ?: return@Observer
                Log.d("test", "sdfdddd ${filteredPostList}")
                updateRecentPost(filteredPostList)
            })
        binding.recentProgressBar.visibility = View.VISIBLE

        setUpSpinner()
        recentPostViewModel.loadRecentPosts()
    }

    fun updateRecentPost(recentPostList: PostResponseList) {
        val adapter = CustomPostAdapter()
        adapter.listData = recentPostList
        adapter.setOnItemClickListener(object: CustomPostAdapter.OnItemClickListener{
            override fun onItemClick(v: View, post: PostResponse, pos: Int) {
                if (post.id != null) {
                    (activity as MainActivity).goCommunityDetailActivity(post.id)
                }
            }
        })
        binding.recentRecyclerView.adapter = adapter
        binding.recentRecyclerView.layoutManager = StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL)
        binding.recentProgressBar.visibility = View.GONE
        binding.recentPostCountTextView.text = "총 ${recentPostList.size.toString()}개 "
    }

    fun setUpSpinner() {
        val categories = resources.getStringArray(R.array.post_category)
        binding.autoCompleteTextView.setText("전체")
        val adapter = ArrayAdapter((activity as MainActivity), R.layout.support_simple_spinner_dropdown_item, categories)
        binding.autoCompleteTextView.setAdapter(adapter)
        binding.autoCompleteTextView.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                println("출력 테스트: " + position)
                recentPostViewModel.filterPost(position)
            }

        }
    }

}