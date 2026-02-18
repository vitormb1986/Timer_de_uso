package com.timerdeuso.app.service

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.util.Log
import com.timerdeuso.app.data.repository.AppRepository
import com.timerdeuso.app.util.NotificationHelper
import com.timerdeuso.app.util.PrefsManager
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class UsageMonitorService : Service() {

    companion object {
        private const val TAG = "UsageMonitorService"
        private const val CHECK_INTERVAL_SECONDS = 3L

        fun start(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, UsageMonitorService::class.java))
        }
    }

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var checkTask: ScheduledFuture<*>? = null
    private lateinit var repository: AppRepository
    private lateinit var usageStatsManager: UsageStatsManager

    // Per-app session tracking
    private val sessionStartTimes = mutableMapOf<String, Long>()      // When app session started
    private val accumulatedTimes = mutableMapOf<String, Long>()        // Accumulated ms in this session
    private val warningShown = mutableSetOf<String>()                  // 80% warning shown
    private val alarmFired = mutableSetOf<String>()                    // 100% alarm fired
    private val snoozedUntil = mutableMapOf<String, Long>()           // Snooze end time

    private var lastForegroundPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        ServiceHolder.instance = this
        repository = AppRepository(applicationContext)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NotificationHelper.NOTIFICATION_SERVICE_ID,
            NotificationHelper.buildServiceNotification(this))
        PrefsManager.setServiceRunning(this, true)

        checkTask?.cancel(false)
        checkTask = executor.scheduleAtFixedRate(
            ::checkUsage, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS
        )

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkUsage() {
        try {
            val currentApp = getForegroundApp() ?: return
            val monitoredApps = repository.getActiveAppsSync()
            val monitoredMap = monitoredApps.associateBy { it.packageName }
            val now = System.currentTimeMillis()

            // If user switched apps, close old session and start new
            if (currentApp != lastForegroundPackage) {
                lastForegroundPackage?.let { oldPkg ->
                    // Save accumulated time for old app
                    sessionStartTimes[oldPkg]?.let { start ->
                        val elapsed = now - start
                        accumulatedTimes[oldPkg] = (accumulatedTimes[oldPkg] ?: 0) + elapsed
                    }
                    sessionStartTimes.remove(oldPkg)
                }

                // If new app is NOT monitored, just reset tracking for the old one
                if (currentApp !in monitoredMap) {
                    // Clear session data for unmonitored apps that we left
                    lastForegroundPackage?.let { oldPkg ->
                        if (oldPkg !in monitoredMap) {
                            resetSession(oldPkg)
                        }
                    }
                    lastForegroundPackage = currentApp
                    return
                }

                // Reset session for the newly opened monitored app (timer resets on re-entry)
                // Only reset if the app was NOT in a snoozed state
                if (currentApp !in snoozedUntil || now > (snoozedUntil[currentApp] ?: 0)) {
                    if (accumulatedTimes.containsKey(currentApp) && lastForegroundPackage != null) {
                        // User left and came back - reset session
                        resetSession(currentApp)
                    }
                }

                sessionStartTimes[currentApp] = now
                lastForegroundPackage = currentApp
                return
            }

            // Same app as before - check if it's monitored
            val monitored = monitoredMap[currentApp] ?: return

            // Ensure we have a session start
            if (currentApp !in sessionStartTimes) {
                sessionStartTimes[currentApp] = now
            }

            val sessionElapsed = (accumulatedTimes[currentApp] ?: 0) +
                    (now - (sessionStartTimes[currentApp] ?: now))
            val limitMs = monitored.timeLimitMinutes * 60_000L

            // Check if snoozed
            snoozedUntil[currentApp]?.let { snoozeEnd ->
                if (now < snoozeEnd) return
                // Snooze expired, remove it
                snoozedUntil.remove(currentApp)
            }

            // 80% warning
            if (sessionElapsed >= limitMs * 0.8 && currentApp !in warningShown && currentApp !in alarmFired) {
                warningShown.add(currentApp)
                vibrateLight()
                NotificationHelper.showWarningNotification(
                    this, currentApp, monitored.appName,
                    (sessionElapsed / 60_000).toInt(), monitored.timeLimitMinutes
                )
            }

            // 100% alarm
            if (sessionElapsed >= limitMs && currentApp !in alarmFired) {
                alarmFired.add(currentApp)
                vibrateStrong()
                NotificationHelper.showAlarmNotification(
                    this, currentApp, monitored.appName, monitored.timeLimitMinutes
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking usage", e)
        }
    }

    private fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST, now - 10_000, now
        )
        if (stats.isNullOrEmpty()) return null

        return stats
            .filter { it.lastTimeUsed > 0 }
            .maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    private fun resetSession(packageName: String) {
        sessionStartTimes.remove(packageName)
        accumulatedTimes.remove(packageName)
        warningShown.remove(packageName)
        alarmFired.remove(packageName)
        snoozedUntil.remove(packageName)
        NotificationHelper.dismissAlarmNotification(this, packageName)
    }

    fun snoozeApp(packageName: String) {
        val snoozeMs = PrefsManager.getSnoozeMinutes(this) * 60_000L
        snoozedUntil[packageName] = System.currentTimeMillis() + snoozeMs
        alarmFired.remove(packageName)
        warningShown.remove(packageName)
        NotificationHelper.dismissAlarmNotification(this, packageName)
    }

    fun silenceApp(packageName: String) {
        repository.silenceUntilMidnight(packageName)
        resetSession(packageName)
    }

    // Allow receiver to access the service instance
    object ServiceHolder {
        var instance: UsageMonitorService? = null
    }

    override fun onDestroy() {
        super.onDestroy()
        checkTask?.cancel(false)
        ServiceHolder.instance = null
        PrefsManager.setServiceRunning(this, false)
    }

    private fun vibrateLight() {
        val vibrator = getVibratorCompat()
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun vibrateStrong() {
        val vibrator = getVibratorCompat()
        vibrator.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1)
        )
    }

    private fun getVibratorCompat(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
