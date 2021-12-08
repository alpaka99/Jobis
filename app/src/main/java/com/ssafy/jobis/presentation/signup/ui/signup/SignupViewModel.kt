package com.ssafy.jobis.presentation.signup.ui.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.ssafy.jobis.R
import com.ssafy.jobis.presentation.signup.data.SignupRepository
import com.ssafy.jobis.presentation.signup.data.Result
import kotlinx.coroutines.*

class SignupViewModel(private val signupRepository: SignupRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<SignupFormState>()
    val signupFormState: LiveData<SignupFormState> = _loginForm

    private val _loginResult = MutableLiveData<SignupResult>()
    val signupResult: LiveData<SignupResult> = _loginResult

    fun login(username: String, nickname: String, password: String) {
        // can be launched in a separate asynchronous job
        CoroutineScope(Dispatchers.Main).launch {
//            val job1 = CoroutineScope(Dispatchers.IO).async {
//                signupRepository.signup(username, password)
//            }
//            val job2 = CoroutineScope(Dispatchers.IO).async {
//                signupRepository.createAccount(username, nickname, password)
//            }
            val job1 = signupRepository.signup(username, password)
            if (job1) {
                val job2 = signupRepository.createAccount(username, nickname, password)
                if (job2 is Result.Success) {
                    _loginResult.value =
                        SignupResult(success = SignedUpUserView(displayName = "회원가입 성공"))
                } else {
                    _loginResult.value = SignupResult(error = R.string.signup_failed)
                }
            } else {
                _loginResult.value = SignupResult(error = R.string.signup_failed)
            }

        }
    }

    fun loginDataChanged(username: String, nickname: String, password: String, passwordConfirmation: String) {
        if (!isUserNameValid(username)) {
            Log.d("test", "아이디!")
            _loginForm.value = SignupFormState(usernameError = R.string.invalid_username)
        } else if (!isNickNameValid(nickname)) {
            Log.d("test", "닉네임!!!")
            _loginForm.value = SignupFormState(nicknameError = R.string.invalid_nickname)
        } else if (!isPasswordValid(password)) {
            Log.d("test", "비밀번호!!!")
            _loginForm.value = SignupFormState(passwordError = R.string.invalid_password)
        } else if (!isPasswordConfirmationValid(password, passwordConfirmation)) {
            Log.d("test", "비밀번호확인!!!")
            _loginForm.value = SignupFormState(passwordConfirmationError = R.string.invalid_password_comfirmation)
        } else {
            Log.d("test", "다 맞음!!!")
            _loginForm.value = SignupFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isNickNameValid(nickname: String): Boolean {
        val trimmedNickName = nickname.replace(" ", "")
        return trimmedNickName.length >= 2
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPasswordConfirmationValid(password: String, passwordConfirmation: String): Boolean {
        Log.d("test", "${password} ${passwordConfirmation} ${password == passwordConfirmation}")
        return password == passwordConfirmation
    }
}