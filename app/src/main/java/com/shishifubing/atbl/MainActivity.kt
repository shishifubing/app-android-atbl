package com.shishifubing.atbl

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherAppsSerializer
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import com.shishifubing.atbl.domain.SettingsSerializer
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
    private lateinit var launcherAppsRepo: LauncherAppsRepository
    private lateinit var launcherSettingsRepo: LauncherSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launcherAppsManager = LauncherAppsManager(
            this, lifecycle
        )
        launcherAppsRepo = LauncherAppsRepository(
            launcherAppsDataStore, this
        )
        launcherSettingsRepo = LauncherSettingsRepository(settingsDataStore)

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
        lifecycleScope.launch { launcherSettingsRepo.fetchInitial() }

        val vm = ViewModelProvider(
            this,
            LauncherViewModelFactory(
                launcherSettingsRepo, launcherAppsRepo, launcherAppsManager,
                runBlocking { launcherSettingsRepo.fetchInitial() }
            )
        )[LauncherViewModel::class.java]

        setContent {
            LauncherTheme {
                Surface {
                    LauncherScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                        vm = vm
                    )
                }
            }
        }
    }

    override fun onResume() {
        lifecycleScope.launch {
            launcherAppsRepo.updateIsHomeApp(launcherAppsManager.isHomeApp())
        }
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        launcherAppsManager.removeCallbacks()
    }
}


