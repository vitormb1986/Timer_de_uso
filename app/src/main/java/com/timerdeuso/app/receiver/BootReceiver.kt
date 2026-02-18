package com.timerdeuso.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timerdeuso.app.service.UsageMonitorService
import com.timerdeuso.app.util.PrefsManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (PrefsManager.isServiceRunning(context)) {
                UsageMonitorService.start(context)
            }
            MidnightResetReceiver.scheduleMidnightReset(context)
        }
    }
}
