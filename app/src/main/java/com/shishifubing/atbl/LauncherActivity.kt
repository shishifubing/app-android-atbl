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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcherAppsManager = LauncherAppsManager(this)
        val settingsRepo = LauncherSettingsRepository(settingsDataStore)
        val launcherAppsRepo = LauncherAppsRepository(
            launcherAppsDataStore, this
        )
        val vm = ViewModelProvider(
            this,
            LauncherViewModelFactory(
                settingsRepo, launcherAppsRepo, launcherAppsManager,
                runBlocking { settingsRepo.fetchInitial() },
                runBlocking { launcherAppsRepo.reloadApps() }
            )
        )[LauncherViewModel::class.java]

        launcherAppsManager.addCallback {
            lifecycleScope.launch { launcherAppsRepo.reloadApps() }
        }

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
}


