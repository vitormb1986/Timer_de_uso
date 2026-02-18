package com.timerdeuso.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.timerdeuso.app.data.model.AppInfo
import com.timerdeuso.app.data.model.MonitoredApp
import com.timerdeuso.app.data.repository.AppRepository
import com.timerdeuso.app.util.PrefsManager
import java.util.concurrent.Executors

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val executor = Executors.newSingleThreadExecutor()

    val monitoredApps: LiveData<List<MonitoredApp>> = repository.monitoredApps

    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadInstalledApps() {
        _isLoading.postValue(true)
        executor.execute {
            val apps = repository.getInstalledApps()
            _installedApps.postValue(apps)
            _isLoading.postValue(false)
        }
    }

    fun addMonitoredApp(packageName: String, appName: String, timeLimitMinutes: Int) {
        repository.addMonitoredApp(packageName, appName, timeLimitMinutes)
    }

    fun removeMonitoredApp(packageName: String) {
        repository.removeMonitoredApp(packageName)
    }

    fun updateTimeLimit(packageName: String, minutes: Int) {
        repository.updateTimeLimit(packageName, minutes)
    }

    fun getSnoozeMinutes(): Int =
        PrefsManager.getSnoozeMinutes(getApplication())

    fun setSnoozeMinutes(minutes: Int) {
        PrefsManager.setSnoozeMinutes(getApplication(), minutes)
    }
}
