package com.ssafy.jobis.presentation.community.search.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssafy.jobis.data.repository.CommunityRepository
import com.ssafy.jobis.data.response.PostResponse
import com.ssafy.jobis.data.response.PostResponseList
import com.ssafy.jobis.databinding.CommunitySearchFragmentBinding
import com.ssafy.jobis.presentation.MainActivity
import com.ssafy.jobis.presentation.community.CustomPostAdapter
import com.ssafy.jobis.presentation.community.search.CommunitySearchActivity
import com.ssafy.jobis.presentation.community.search.CommunitySearchViewModel
import java.lang.IllegalArgumentException

class CommunitySearchFragment : Fragment() {

    private val activityViewModel: CommunitySearchViewModel by activityViewModels {
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

    private var _binding: CommunitySearchFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = CommunitySearchFragment()
    }

    private lateinit var viewModel: CommunitySearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CommunitySearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.postList.observe(viewLifecycleOwner,
            Observer { postList ->
                postList ?: return@Observer
                updateSearchedPost(postList)
            })

    }

    fun updateSearchedPost(postList: MutableList<PostResponse>) {
        val adapter = CustomPostAdapter()
        adapter.listData = postList
        adapter.setOnItemClickListener(object: CustomPostAdapter.OnItemClickListener{
            override fun onItemClick(v: View, post: PostResponse, pos: Int) {
                if (post.id != null) {
                    (activity as CommunitySearchActivity).goCommunityDetailActivity(post.id)
                }
            }
        })
        binding.communitySearchRecyclerView.adapter = adapter
        binding.communitySearchRecyclerView.layoutManager = StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL)
    }

}