package com.shishifubing.atbl

import android.appwidget.AppWidgetHost
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherAppsSerializer
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import com.shishifubing.atbl.domain.LauncherSettingsSerializer
import com.shishifubing.atbl.ui.LauncherTheme
import com.shishifubing.atbl.ui.UI
import kotlinx.coroutines.launch

private val tag = MainActivity::class.simpleName

val Context.settingsDataStore: DataStore<LauncherSettings> by dataStore(
    fileName = "settings.pb",
    serializer = LauncherSettingsSerializer
)

val Context.launcherAppsDataStore: DataStore<LauncherApps> by dataStore(
    fileName = "launcherApps.pb",
    serializer = LauncherAppsSerializer
)

class MainActivity : ComponentActivity() {

    private lateinit var app: LauncherApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as LauncherApplication
        app.appWidgetHost = AppWidgetHost(this, 0)
        app.settingsRepo =
            LauncherSettingsRepository(settingsDataStore)
        app.appsRepo = LauncherAppsRepository(
            launcherAppsDataStore,
            this
        )
        app.appsManager = LauncherAppsManager(this)
        val manager = app.appsManager!!
        val appsRepo = app.appsRepo!!

        manager.addCallback(
            onChanged = { packageName ->
                lifecycleScope.launch { appsRepo.reloadApp(packageName) }
            },
            onRemoved = { packageName ->
                lifecycleScope.launch { appsRepo.removeApp(packageName) }
            }
        )
        lifecycleScope.launch { appsRepo.fetchInitial() }
        setContent {
            LauncherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UI(modifier = Modifier.safeDrawingPadding())
                }
            }
        }
    }

    override fun onResume() {
        lifecycleScope.launch {
            app.appsRepo!!.updateIsHomeApp(app.appsManager!!.isHomeApp())
        }
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        app.appsManager!!.removeCallbacks()
    }
}


