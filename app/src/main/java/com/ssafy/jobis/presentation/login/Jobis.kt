package com.ssafy.jobis.presentation.login

import android.app.Application
import android.content.Context
import android.util.Log

class Jobis: Application() {

    init {
        instance = this
    }

    companion object {
        var instance: Jobis? = null
        lateinit var prefs: Preference
        fun context(): Context = instance!!.applicationContext
    }

    override fun onCreate() {
        prefs = Preference(applicationContext)
        super.onCreate()
    }
}

