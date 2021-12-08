package com.ssafy.jobis.presentation.signup.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ssafy.jobis.presentation.signup.data.SignupDataSource
import com.ssafy.jobis.presentation.signup.data.SignupRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
// viewmodelfactory를 이용해서 초기화해야 viewmodel에서 액티비티의 생명주기를 사용할 수 있다.
class SignupViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            return SignupViewModel(
                signupRepository = SignupRepository(
                    dataSource = SignupDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}