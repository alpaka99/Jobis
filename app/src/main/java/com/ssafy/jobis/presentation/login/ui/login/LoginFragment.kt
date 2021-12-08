package com.ssafy.jobis.presentation.login.ui.login

import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.ssafy.jobis.R
import com.ssafy.jobis.databinding.FragmentLoginBinding
import com.ssafy.jobis.presentation.login.UserActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    private var userActivity: UserActivity? = null
    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

      _binding = FragmentLoginBinding.inflate(inflater, container, false)
      return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        val usernameEditText = binding.userEmail
        val passwordEditText = binding.userPassword
        val loginButton = binding.login
//        val loadingProgressBar = binding.loading

        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                userActivity?.loadingOff()
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            userActivity?.loadingOn()
//            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        binding.findPasswordButton.setOnClickListener {
            findPassword(binding.userEmail.text.toString())
        }

        binding.signUpButton.setOnClickListener {
            userActivity?.goSignUp()
        }
    }

    private fun findPassword(userEmail: String) {
        val appContext = context?.applicationContext ?: return
        if (loginViewModel.isUserNameValid(userEmail)){
            Firebase.auth.sendPasswordResetEmail(userEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(appContext, "비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(appContext, "비밀번호 재설정 이메일 전송 실패", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(appContext, "이메일을 입력하신 뒤 다시 버튼을 눌러주세요.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = "로그인 되었습니다."
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
        userActivity?.goMain()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UserActivity) userActivity = context
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = Firebase.auth.currentUser
        if(currentUser != null) {
            val appContext = context?.applicationContext ?: return
            Toast.makeText(appContext, "로그인 되었습니다.", Toast.LENGTH_LONG).show()
            userActivity?.goMain()
        }
    }
}