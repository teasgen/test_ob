package com.example.healthmonitor

import android.app.Application
import com.example.healthmonitor.data.auth.AuthManager
import com.example.healthmonitor.data.network.NetworkModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkModule.init(this)
        AuthManager.init(this)
    }
}