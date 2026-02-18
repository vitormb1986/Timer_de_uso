package com.timerdeuso.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_apps")
data class MonitoredApp(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val timeLimitMinutes: Int,
    val isEnabled: Boolean = true,
    val isSilencedUntilMidnight: Boolean = false
)
