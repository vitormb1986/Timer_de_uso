package com.timerdeuso.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.timerdeuso.app.data.model.MonitoredApp

@Dao
interface MonitoredAppDao {

    @Query("SELECT * FROM monitored_apps")
    fun getAll(): LiveData<List<MonitoredApp>>

    @Query("SELECT * FROM monitored_apps")
    fun getAllSync(): List<MonitoredApp>

    @Query("SELECT * FROM monitored_apps WHERE isEnabled = 1 AND isSilencedUntilMidnight = 0")
    fun getActiveSync(): List<MonitoredApp>

    @Query("SELECT * FROM monitored_apps WHERE packageName = :packageName")
    fun getByPackage(packageName: String): MonitoredApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(app: MonitoredApp)

    @Delete
    fun delete(app: MonitoredApp)

    @Query("DELETE FROM monitored_apps WHERE packageName = :packageName")
    fun deleteByPackage(packageName: String)

    @Query("UPDATE monitored_apps SET isSilencedUntilMidnight = 1 WHERE packageName = :packageName")
    fun silenceUntilMidnight(packageName: String)

    @Query("UPDATE monitored_apps SET isSilencedUntilMidnight = 0")
    fun resetAllSilenced()

    @Query("UPDATE monitored_apps SET isEnabled = :enabled WHERE packageName = :packageName")
    fun setEnabled(packageName: String, enabled: Boolean)

    @Query("UPDATE monitored_apps SET timeLimitMinutes = :minutes WHERE packageName = :packageName")
    fun updateTimeLimit(packageName: String, minutes: Int)
}
