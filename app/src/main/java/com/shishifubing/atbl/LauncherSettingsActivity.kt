package com.shishifubing.atbl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.ui.LauncherTheme
import com.shishifubing.atbl.ui.SettingsScreen
import com.shishifubing.atbl.ui.SettingsViewModel
import com.shishifubing.atbl.ui.SettingsViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class LauncherSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepo = LauncherSettingsRepository(settingsDataStore)
        val appsRepo = LauncherAppsRepository(launcherAppsDataStore, this)
        lifecycleScope.launch { appsRepo.fetchInitial() }
        lifecycleScope.launch { settingsRepo.fetchInitial() }

        val vm = ViewModelProvider(
            this,
            SettingsViewModelFactory(
                settingsRepo, appsRepo,
                runBlocking { settingsRepo.fetchInitial() }
            )
        )[SettingsViewModel::class.java]

        setContent {
            LauncherTheme {
                Surface {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        vm = vm
                    )
                }
            }
        }
    }
}