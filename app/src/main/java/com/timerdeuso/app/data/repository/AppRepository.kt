package com.timerdeuso.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import com.timerdeuso.app.data.db.AppDatabase
import com.timerdeuso.app.data.model.AppInfo
import com.timerdeuso.app.data.model.MonitoredApp
import java.util.concurrent.Executors

class AppRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).monitoredAppDao()
    private val pm = context.packageManager
    private val executor = Executors.newSingleThreadExecutor()

    val monitoredApps: LiveData<List<MonitoredApp>> = dao.getAll()

    fun getInstalledApps(): List<AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val monitoredMap = dao.getAllSync().associateBy { it.packageName }

        return resolveInfos
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val packageName = appInfo.packageName
                try {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val icon = pm.getApplicationIcon(appInfo)
                    val monitored = monitoredMap[packageName]
                    AppInfo(
                        packageName = packageName,
                        appName = appName,
                        icon = icon,
                        isMonitored = monitored != null,
                        timeLimitMinutes = monitored?.timeLimitMinutes ?: 15
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun addMonitoredApp(packageName: String, appName: String, timeLimitMinutes: Int) {
        executor.execute {
            dao.insert(
                MonitoredApp(
                    packageName = packageName,
                    appName = appName,
                    timeLimitMinutes = timeLimitMinutes,
                    isEnabled = true,
                    isSilencedUntilMidnight = false
                )
            )
        }
    }

    fun removeMonitoredApp(packageName: String) {
        executor.execute { dao.deleteByPackage(packageName) }
    }

    fun updateTimeLimit(packageName: String, minutes: Int) {
        executor.execute { dao.updateTimeLimit(packageName, minutes) }
    }

    fun silenceUntilMidnight(packageName: String) {
        executor.execute { dao.silenceUntilMidnight(packageName) }
    }

    fun resetAllSilenced() {
        executor.execute { dao.resetAllSilenced() }
    }

    fun getActiveAppsSync(): List<MonitoredApp> = dao.getActiveSync()
}
