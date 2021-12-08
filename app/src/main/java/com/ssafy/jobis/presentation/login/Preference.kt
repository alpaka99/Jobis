package com.ssafy.jobis.presentation.login

import android.content.Context
import android.content.Context.MODE_PRIVATE

class Preference(context: Context) {
    private val preference = "preference"
    private val prefs = context.getSharedPreferences(preference, MODE_PRIVATE)

    fun getString(key: String?, defValue: String?): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String?, str: String?) {
        prefs.edit().putString(key, str).apply()
    }
}