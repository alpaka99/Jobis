package com.ssafy.jobis.presentation.community

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ssafy.jobis.databinding.FragmentCommunityBinding
import com.ssafy.jobis.presentation.MainActivity
import com.ssafy.jobis.presentation.community.popular.PopularPostFragment
import com.ssafy.jobis.presentation.community.recent.RecentPostFragment
import com.google.android.material.tabs.TabLayoutMediator

class CommunityFragment: Fragment() {
    private var mainActivity: MainActivity? = null
    private lateinit var communityViewModel: CommunityViewModel
    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentList = listOf(PopularPostFragment(), RecentPostFragment())
        val adapter = CommunityFragmentAdapter(this)
        adapter.fragmentList = fragmentList
        binding.postViewPager.adapter = adapter
        binding.postViewPager.isSaveEnabled = false

        val tabTitles = listOf<String>("인기글", "최신글")
        TabLayoutMediator(binding.postTabLayout, binding.postViewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.postCreateButton.setOnClickListener {
            mainActivity?.goPostCreateFragment()
        }

        binding.communitySearchButton.setOnClickListener {
            mainActivity?.goCommunitySearchActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) mainActivity = context
    }

}
