package com.timerdeuso.app.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isMonitored: Boolean = false,
    val timeLimitMinutes: Int = 15
)
