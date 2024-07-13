package cn.bincker.android_ddns

import android.app.Application

class App: Application() {
    companion object {
        private lateinit var instance: App

        fun getContext() = instance.applicationContext
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}