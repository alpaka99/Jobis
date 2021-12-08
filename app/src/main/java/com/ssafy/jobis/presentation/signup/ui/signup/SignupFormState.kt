package com.ssafy.jobis.presentation.signup.ui.signup

/**
 * Data validation state of the login form.
 */
data class SignupFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val passwordConfirmationError: Int? = null,
    val nicknameError: Int? = null,
    val isDataValid: Boolean = false
)