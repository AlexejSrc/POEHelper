package com.resdev.poehelper.view

import android.app.Application
import android.content.Context
import android.view.ContextThemeWrapper

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        CONTEXT = applicationContext
    }
    companion object{
        private lateinit var CONTEXT: Context
        fun getApplicationContext() = CONTEXT
    }
}