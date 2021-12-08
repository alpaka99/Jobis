package com.ssafy.jobis.presentation.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.ssafy.jobis.R
import com.ssafy.jobis.data.repository.AdminRepository
import com.ssafy.jobis.databinding.AdminActivityBinding
import com.ssafy.jobis.presentation.admin.report.AdminReportFragment
import com.ssafy.jobis.presentation.admin.user.AdminUserFragment
import com.ssafy.jobis.presentation.community.detail.CommunityDetailActivity
import java.lang.IllegalArgumentException

class AdminActivity : AppCompatActivity() {

    val binding by lazy { AdminActivityBinding.inflate(layoutInflater)}

    private val getResultAdminDetail = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            adminViewModel.loadAllReport()
        }
    }

    private val adminViewModel: AdminViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                    return AdminViewModel(
                        adminRepository = AdminRepository()
                    ) as T
                }
                throw IllegalArgumentException("Unkown ViewModel Class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.adminFrame, AdminUserFragment())
            .commit()
        binding.adminTabLayout.getTabAt(0)?.text = "유저 목록"
        binding.adminTabLayout.getTabAt(1)?.text = "신고 목록"
        binding.adminTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                var pos = tab!!.position
                when(pos) {
                    0 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.adminFrame, AdminUserFragment())
                            .commit()
                    }
                    1 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.adminFrame, AdminReportFragment())
                            .commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        adminViewModel.loadAllUser()
        adminViewModel.loadAllReport()
    }

    fun goCommunityDetailActivity(post_id: String) {
        val intent = Intent(this, CommunityDetailActivity::class.java)
        intent.putExtra("id", post_id)
        getResultAdminDetail.launch(intent)
    }
}