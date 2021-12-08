package com.ssafy.jobis.presentation.signup.data

import android.util.Log
import com.ssafy.jobis.presentation.signup.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class SignupRepository(val dataSource: SignupDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    suspend fun signup(username: String, password: String): Boolean {
        return dataSource.signup(username, password)
    }

    suspend fun createAccount(username: String, nickname: String, password: String): Result<String>? {
        return dataSource.saveAccount(username, nickname, password)
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}