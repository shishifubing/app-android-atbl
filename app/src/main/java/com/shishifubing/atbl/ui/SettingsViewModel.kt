package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SettingsViewModel(
    private val settingsRepo: LauncherSettingsRepository,
    private val appsRepo: LauncherAppsRepository
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LauncherApplication
                SettingsViewModel(
                    settingsRepo = app.settingsRepo!!,
                    appsRepo = app.appsRepo!!
                )
            }
        }
    }

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }

    val uiState = combine(
        appsRepo.appsFlow,
        settingsRepo.settingsFlow
    ) { apps, settings ->
        SettingsScreenUiState.Success(
            apps = apps.appsMap.values,
            splitScreenShortcuts = apps.splitScreenShortcutsList,
            settings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsScreenUiState.Loading
    )

    val settingsActions = object : SettingsActions {
        override fun updateSettings(
            action: suspend LauncherSettings.Builder.() -> LauncherSettings.Builder
        ) {
            launch { settingsRepo.update { runBlocking { action(it) } } }
        }

        override fun updateSettingsFromBytes(bytes: ByteArray) {
            launch { settingsRepo.updateFromBytes(bytes) }
        }

        override fun backupReset() {
            updateSettings { settingsRepo.getDefault() }
        }

        override fun setHiddenApps(packageNames: List<String>) {
            launch { appsRepo.setHiddenApps(packageNames) }
        }

        override fun addSplitScreenShortcut(
            appTop: LauncherApp,
            appBottom: LauncherApp
        ) {
            launch {
                appsRepo.addSplitScreenShortcut(
                    appTop.packageName,
                    appBottom.packageName
                )
            }
        }

        override fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
            launch {
                appsRepo.removeSplitScreenShortcut(shortcut)
            }
        }

    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }
}

sealed interface SettingsScreenUiState {
    data class Success(
        val settings: LauncherSettings,
        val apps: Collection<LauncherApp>,
        val splitScreenShortcuts: List<LauncherSplitScreenShortcut>
    ) : SettingsScreenUiState

    object Loading : SettingsScreenUiState
}

interface SettingsActions {
    fun updateSettings(action: suspend LauncherSettings.Builder.() -> LauncherSettings.Builder)

    fun updateSettingsFromBytes(bytes: ByteArray)

    fun backupReset()

    fun setHiddenApps(packageNames: List<String>)

    fun addSplitScreenShortcut(appTop: LauncherApp, appBottom: LauncherApp)
    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut)
}