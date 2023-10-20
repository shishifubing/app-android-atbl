package com.shishifubing.atbl

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.ui.LauncherScreen
import com.shishifubing.atbl.ui.LauncherTheme
import com.shishifubing.atbl.ui.LauncherViewModel
import com.shishifubing.atbl.ui.LauncherViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val tag = LauncherActivity::class.simpleName

val Context.settingsDataStore: DataStore<LauncherSettings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer
)

val Context.launcherAppsDataStore: DataStore<LauncherApps> by dataStore(
    fileName = "launcherApps.pb",
    serializer = LauncherAppsSerializer
)

class LauncherActivity : ComponentActivity() {
    private lateinit var launcherAppsManager: LauncherAppsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launcherAppsManager = LauncherAppsManager(this)
        val settingsRepo = LauncherSettingsRepository(settingsDataStore)
        val launcherAppsRepo = LauncherAppsRepository(
            launcherAppsDataStore, this
        )
        launcherAppsManager.addCallback(
            onAdded = { packageName ->
                lifecycleScope.launch { launcherAppsRepo.addApp(packageName) }
            },
            onChanged = { packageName ->
                lifecycleScope.launch { launcherAppsRepo.updateApp(packageName) }
            },
            onRemoved = { packageName ->
                lifecycleScope.launch { launcherAppsRepo.removeApp(packageName) }
            }
        )

        lifecycleScope.launch { launcherAppsRepo.fetchInitial() }
        lifecycleScope.launch { settingsRepo.fetchInitial() }

        val vm = ViewModelProvider(
            this,
            LauncherViewModelFactory(
                settingsRepo, launcherAppsRepo, launcherAppsManager,
                runBlocking { settingsRepo.fetchInitial() },
                runBlocking { launcherAppsRepo.fetchInitial() }
            )
        )[LauncherViewModel::class.java]

        setContent {
            LauncherTheme {
                Surface {
                    LauncherScreen(
                        modifier = Modifier.fillMaxSize(),
                        vm = vm
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        launcherAppsManager.removeCallbacks()
    }
}


