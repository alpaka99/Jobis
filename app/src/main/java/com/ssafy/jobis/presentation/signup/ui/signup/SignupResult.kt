package com.ssafy.jobis.presentation.signup.ui.signup

/**
 * Authentication result : success (user details) or error message.
 */
data class SignupResult(
    val success: SignedUpUserView? = null,
    val error: Int? = null
)