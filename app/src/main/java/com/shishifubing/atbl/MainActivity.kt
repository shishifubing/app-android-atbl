package com.shishifubing.atbl

import android.appwidget.AppWidgetHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.ui.LauncherThemeWithSurface
import com.shishifubing.atbl.ui.UI
import kotlinx.coroutines.launch

private val tag = MainActivity::class.simpleName


class MainActivity : ComponentActivity() {

    private lateinit var app: LauncherApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = LauncherManager(this, lifecycle)
        val stateRepo = LauncherStateRepository(manager, this)
        val settingsRepo = LauncherSettingsRepository(this)
        val appWidgetHost = AppWidgetHost(this, 0)

        // needed for view models
        app = (application as LauncherApplication).apply {
            this.settingsRepo = settingsRepo
            this.appsManager = manager
            this.stateRepo = stateRepo
            this.appWidgetHost = appWidgetHost
        }

        lifecycleScope.launch {
            stateRepo.updateState()
        }
        manager.addCallback(
            onChanged = { packageName ->
                lifecycleScope.launch { stateRepo.reloadApp(packageName) }
            },
            onRemoved = { packageName ->
                lifecycleScope.launch { stateRepo.removeApp(packageName) }
            }
        )

        setContent {
            LauncherThemeWithSurface {
                UI()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch { app.stateRepo!!.updateIsHomeApp() }
    }

    override fun onStop() {
        super.onStop()

        app.appsManager!!.removeCallbacks()
    }
}


