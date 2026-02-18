package com.timerdeuso.app

import android.app.Application
import com.timerdeuso.app.util.NotificationHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
