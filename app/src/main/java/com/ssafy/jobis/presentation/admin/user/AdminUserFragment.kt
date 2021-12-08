package com.ssafy.jobis.presentation.admin.user

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssafy.jobis.R
import com.ssafy.jobis.data.repository.AdminRepository
import com.ssafy.jobis.data.response.UserResponse
import com.ssafy.jobis.databinding.FragmentAdminReportBinding
import com.ssafy.jobis.databinding.FragmentAdminUserBinding
import com.ssafy.jobis.presentation.admin.AdminActivity
import com.ssafy.jobis.presentation.admin.AdminViewModel
import com.ssafy.jobis.presentation.community.detail.CommunityDetailActivity
import java.lang.IllegalArgumentException

class AdminUserFragment : Fragment() {

    private val activityViewModel: AdminViewModel by activityViewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                    return AdminViewModel(
                        adminRepository = AdminRepository()
                    ) as T
                }
                throw IllegalArgumentException("UnKnown ViewModel Class")
            }
        }
    }

    private var _binding: FragmentAdminUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.userList.observe(viewLifecycleOwner,
            Observer { userList ->
                userList ?: return@Observer
                updateUserList(userList)
            })
    }

    private fun updateUserList(userList: MutableList<UserResponse>) {
        val adapter = AdminUserAdapter()
        adapter.listData = userList
        adapter.setOnItemClickListener(object: AdminUserAdapter.OnItemClickListener{
            override fun onItemClick(v: View, user: UserResponse, pos: Int) {
//                showPopup(user)
                Toast.makeText(context, "구현 중입니다...", Toast.LENGTH_LONG).show()
            }
        })
        binding.userRecyclerView.adapter = adapter
        binding.userRecyclerView.layoutManager = StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL)
    }

    private fun showPopup(user: UserResponse) {
        val inflater = (activity as AdminActivity).getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.select_popup, null)
        val alertDialog = AlertDialog.Builder((activity as AdminActivity))
        alertDialog.setTitle("해당 유저를 삭제하시겠습니까?")
            .setPositiveButton("네", DialogInterface.OnClickListener { dialogInterface, i ->
//                activityViewModel.deleteUser(user)
            })
            .setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
                return@OnClickListener
            })
            .create()
        alertDialog.setView(view)
        alertDialog.show()
    }
}