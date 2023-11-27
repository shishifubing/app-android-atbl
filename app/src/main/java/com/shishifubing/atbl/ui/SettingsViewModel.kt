package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSettingsRepository
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val settingsRepo: LauncherSettingsRepository,
    private val stateRepo: LauncherStateRepository
) : ViewModel() {
    companion object {
        val Factory = launcherViewModelFactory {
            SettingsViewModel(
                settingsRepo = settingsRepo,
                stateRepo = stateRepo
            )
        }
    }

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }

    val uiState = combine(
        stateRepo.stateFlow,
        settingsRepo.settingsFlow
    ) { state, settings ->
        SettingsScreenUiState.Success(
            apps = state.appsMap.values,
            splitScreenShortcuts = state.splitScreenShortcutsMap.values.sortedBy { it.appTop.packageName },
            settings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsScreenUiState.Loading
    )

    val settingsActions = object : SettingsActions {
        override fun updateSettings(
            action: LauncherSettings.Builder.() -> Unit
        ) = settingsAction { update(action) }

        override fun updateSettingsFromBytes(
            bytes: ByteArray
        ) = settingsAction { updateFromBytes(bytes) }

        override fun backupReset() = updateSettings {
            LauncherSettingsRepository.default
        }

        override fun setHiddenApps(packageNames: List<String>) = stateAction {
            setHiddenApps(packageNames)
        }

        override fun addSplitScreenShortcut(
            appTop: LauncherApp, appBottom: LauncherApp
        ) = stateAction {
            addSplitScreenShortcut(appTop.packageName, appBottom.packageName)
        }

        override fun removeSplitScreenShortcut(
            shortcut: LauncherSplitScreenShortcut
        ) = stateAction { removeSplitScreenShortcut(shortcut) }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun settingsAction(action: suspend LauncherSettingsRepository.() -> Unit) {
        launch { action.invoke(settingsRepo) }
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
    fun updateSettings(action: LauncherSettings.Builder.() -> Unit)

    fun updateSettingsFromBytes(bytes: ByteArray)

    fun backupReset()

    fun setHiddenApps(packageNames: List<String>)

    fun addSplitScreenShortcut(appTop: LauncherApp, appBottom: LauncherApp)
    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut)
}