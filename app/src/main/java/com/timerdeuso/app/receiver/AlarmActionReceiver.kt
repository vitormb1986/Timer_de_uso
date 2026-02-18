package com.timerdeuso.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timerdeuso.app.service.UsageMonitorService
import com.timerdeuso.app.util.NotificationHelper

class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("package_name") ?: return
        val service = UsageMonitorService.ServiceHolder.instance

        when (intent.action) {
            "com.timerdeuso.ACTION_SNOOZE" -> {
                service?.snoozeApp(packageName)
                    ?: NotificationHelper.dismissAlarmNotification(context, packageName)
            }
            "com.timerdeuso.ACTION_SILENCE" -> {
                service?.silenceApp(packageName)
                    ?: NotificationHelper.dismissAlarmNotification(context, packageName)
            }
        }
    }
}
