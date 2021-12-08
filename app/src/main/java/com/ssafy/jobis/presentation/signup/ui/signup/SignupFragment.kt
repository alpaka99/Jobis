package com.ssafy.jobis.presentation.signup.ui.signup

import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.ssafy.jobis.databinding.FragmentSignupBinding
import com.ssafy.jobis.presentation.login.UserActivity
import java.util.regex.Matcher
import java.util.regex.Pattern

class SignupFragment : Fragment() {
    private var userActivty: UserActivity? = null
    private lateinit var signupViewModel: SignupViewModel
    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signupViewModel = ViewModelProvider(this, SignupViewModelFactory())
            .get(SignupViewModel::class.java)

        val usernameEditText = binding.userEmail
        val passwordEditText = binding.userPassword
        val passwordConfirmationEditText = binding.userPasswordConfirmation
        val nicknameEditText = binding.userNickName
        val signupButton = binding.login
//        val loadingProgressBar = binding.loading
        signupViewModel.signupFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                signupButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.nicknameError?.let {
                    nicknameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
                loginFormState.passwordConfirmationError?.let {
                    passwordConfirmationEditText.error = getString(it)
                }
            })

        signupViewModel.signupResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                userActivty?.loadingOff()
//                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showSignupFailed(it)
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
                signupViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    nicknameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    passwordConfirmationEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        nicknameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordConfirmationEditText.addTextChangedListener(afterTextChangedListener)
        passwordConfirmationEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signupViewModel.login(
                    usernameEditText.text.toString(),
                    nicknameEditText.text.toString(),
                    passwordEditText.text.toString(),
                )
            }
            false
        }

        signupButton.setOnClickListener {
            userActivty?.loadingOn()
//            loadingProgressBar.visibility = View.VISIBLE
            signupViewModel.login(
                usernameEditText.text.toString(),
                nicknameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        // 뒤로가기 버튼
        binding.signupBackButton.setOnClickListener {
            userActivty?.goLogin()
        }

        val pattern = Pattern.compile("개인정보처리방침")
        val transformFilter = Linkify.TransformFilter(object: Linkify.TransformFilter, (Matcher, String) -> String {
            override fun transformUrl(p0: Matcher?, p1: String?): String {
                return ""
            }

            override fun invoke(p1: Matcher, p2: String): String {
                return ""
            }
        })
        Linkify.addLinks(binding.textView10, pattern, "https://sites.google.com/view/jobis-policy", null, transformFilter)
    }

    private fun updateUiWithUser(model: SignedUpUserView) {
        val welcome = model.displayName
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
        userActivty?.goLogin()
    }

    private fun showSignupFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UserActivity ) {
            userActivty = context
        }
    }
}