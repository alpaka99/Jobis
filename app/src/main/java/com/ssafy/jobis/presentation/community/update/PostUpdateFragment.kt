package com.ssafy.jobis.presentation.community.update

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssafy.jobis.R

class PostUpdateFragment : Fragment() {

    companion object {
        fun newInstance() = PostUpdateFragment()
    }

    private lateinit var viewModel: PostUpdateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.post_update_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PostUpdateViewModel::class.java)
        // TODO: Use the ViewModel
    }

}