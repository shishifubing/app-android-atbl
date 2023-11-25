package com.shishifubing.atbl

import android.appwidget.AppWidgetHost
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import com.shishifubing.atbl.domain.LauncherSettingsSerializer
import com.shishifubing.atbl.domain.LauncherStateRepository
import com.shishifubing.atbl.domain.LauncherStateSerializer
import com.shishifubing.atbl.ui.LauncherTheme
import com.shishifubing.atbl.ui.UI
import kotlinx.coroutines.launch

private val tag = MainActivity::class.simpleName

val Context.settingsDataStore: DataStore<LauncherSettings> by dataStore(
    fileName = "settings.pb",
    serializer = LauncherSettingsSerializer
)

val Context.launcherAppsDataStore: DataStore<LauncherState> by dataStore(
    fileName = "launcherApps.pb",
    serializer = LauncherStateSerializer
)

class MainActivity : ComponentActivity() {

    private lateinit var app: LauncherApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = LauncherAppsManager(this)
        val stateRepo = LauncherStateRepository(launcherAppsDataStore, manager)
        val settingsRepo = LauncherSettingsRepository(settingsDataStore)
        val appWidgetHost = AppWidgetHost(this, 0)

        app = (application as LauncherApplication).apply {
            this.settingsRepo = settingsRepo
            this.appsManager = manager
            this.stateRepo = stateRepo
            this.appWidgetHost = appWidgetHost
        }

        lifecycleScope.launch { stateRepo.updateState() }
        manager.addCallback(
            onChanged = { packageName ->
                lifecycleScope.launch { stateRepo.reloadApp(packageName) }
            },
            onRemoved = { packageName ->
                lifecycleScope.launch { stateRepo.removeApp(packageName) }
            }
        )

        setContent {
            LauncherTheme {
                UI()
            }
        }
    }

    override fun onResume() {
        lifecycleScope.launch { app.stateRepo!!.updateIsHomeApp() }
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        app.appsManager!!.removeCallbacks()
    }
}


