package com.timerdeuso.app.ui.main

import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.timerdeuso.app.R
import com.timerdeuso.app.data.model.AppInfo
import com.timerdeuso.app.receiver.MidnightResetReceiver
import com.timerdeuso.app.service.UsageMonitorService
import com.timerdeuso.app.ui.adapter.AppListAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var fab: FloatingActionButton

    private var allApps: List<AppInfo> = emptyList()
    private var currentFilter: String = ""

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Timer de Uso"

        recyclerView = findViewById(R.id.recycler_apps)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)
        fab = findViewById(R.id.fab_toggle_service)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        adapter = AppListAdapter(
            onToggle = { app, isChecked ->
                if (isChecked) {
                    showTimeLimitDialog(app)
                } else {
                    viewModel.removeMonitoredApp(app.packageName)
                    refreshList()
                }
            },
            onTimeTap = { app ->
                showTimeLimitDialog(app)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.installedApps.observe(this) { apps ->
            allApps = apps
            applyFilter()
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.monitoredApps.observe(this) {
            // Reload list to sync checkbox states
            viewModel.loadInstalledApps()
            updateFab()
        }

        fab.setOnClickListener { toggleService() }

        checkPermissions()
        MidnightResetReceiver.scheduleMidnightReset(this)
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageStatsPermission()) {
            viewModel.loadInstalledApps()
        }
        updateFab()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Buscar app..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilter = newText.orEmpty()
                applyFilter()
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_snooze_settings -> {
                showSnoozeSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applyFilter() {
        val filtered = if (currentFilter.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(currentFilter, ignoreCase = true) ||
                it.packageName.contains(currentFilter, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        emptyText.visibility = if (filtered.isEmpty() && !viewModel.isLoading.value!!) View.VISIBLE else View.GONE
    }

    private fun showTimeLimitDialog(app: AppInfo) {
        val picker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 480
            value = app.timeLimitMinutes
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle("Limite para ${app.appName}")
            .setMessage("Tempo limite em minutos:")
            .setView(picker)
            .setPositiveButton("Salvar") { _, _ ->
                val minutes = picker.value
                viewModel.addMonitoredApp(app.packageName, app.appName, minutes)
                refreshList()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSnoozeSettingsDialog() {
        val picker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 60
            value = viewModel.getSnoozeMinutes()
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle("Tempo de Soneca")
            .setMessage("Minutos para adiar o alarme:")
            .setView(picker)
            .setPositiveButton("Salvar") { _, _ ->
                viewModel.setSnoozeMinutes(picker.value)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toggleService() {
        if (com.timerdeuso.app.util.PrefsManager.isServiceRunning(this)) {
            UsageMonitorService.stop(this)
            com.timerdeuso.app.util.PrefsManager.setServiceRunning(this, false)
        } else {
            if (!hasUsageStatsPermission()) {
                requestUsageStatsPermission()
                return
            }
            UsageMonitorService.start(this)
            com.timerdeuso.app.util.PrefsManager.setServiceRunning(this, true)
        }
        updateFab()
    }

    private fun updateFab() {
        val running = com.timerdeuso.app.util.PrefsManager.isServiceRunning(this)
        fab.setImageResource(if (running) R.drawable.ic_stop else R.drawable.ic_play)
        fab.contentDescription = if (running) "Parar monitoramento" else "Iniciar monitoramento"
    }

    private fun refreshList() {
        viewModel.loadInstalledApps()
    }

    private fun checkPermissions() {
        if (!hasUsageStatsPermission()) {
            AlertDialog.Builder(this)
                .setTitle("Permissão Necessária")
                .setMessage("Para monitorar o uso de apps, é necessário conceder a permissão de acesso ao uso. Deseja abrir as configurações?")
                .setPositiveButton("Abrir") { _, _ -> requestUsageStatsPermission() }
                .setNegativeButton("Cancelar", null)
                .setCancelable(false)
                .show()
        } else {
            viewModel.loadInstalledApps()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}
