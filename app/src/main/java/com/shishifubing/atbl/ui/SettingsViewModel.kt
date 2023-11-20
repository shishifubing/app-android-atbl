package com.shishifubing.atbl.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SettingsViewModel(
    private val settingsRepo: LauncherSettingsRepository,
    private val appsRepo: LauncherAppsRepository
) : ViewModel() {

    private val settingsFlow = settingsRepo.settingsFlow
    val shortcutsFlow = appsRepo.appsFlow.map { it.splitScreenShortcutsList }
    val appsFlow = appsRepo.appsFlow.map { it.appsMap.values }
    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }

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

    inner class SettingsField<T>(
        private val flow: Flow<T>,
        private val setter: suspend CoroutineScope.(
            LauncherSettings.Builder
        ) -> (T) -> LauncherSettings.Builder
    ) {
        fun set(newValue: T) = updateSettings { setter(it)(newValue) }

        @Composable
        fun collectAsState(): State<T> {
            return flow.collectAsState(runBlocking { flow.first() })
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    fun updateSettings(
        action: suspend CoroutineScope.(LauncherSettings.Builder) -> (LauncherSettings.Builder)
    ) {
        launch { settingsRepo.update { runBlocking { action(it) } } }
    }

    fun updateSettingsFromBytes(bytes: ByteArray) {
        launch { settingsRepo.updateFromBytes(bytes) }
    }

    val settings = SettingsField(
        flow = settingsFlow, setter = { { newValue -> newValue.toBuilder() } }
    )

    val splitScreenSeparator = SettingsField(
        flow = settingsFlow.map { it.appCardSplitScreenSeparator },
        setter = { it::setAppCardSplitScreenSeparator }
    )

    val appLayoutReverseOrder = SettingsField(
        flow = settingsFlow.map { it.appLayoutReverseOrder },
        setter = { it::setAppLayoutReverseOrder }
    )

    val appLayoutVerticalArrangement = SettingsField(
        flow = settingsFlow.map { it.appLayoutVerticalArrangement },
        setter = { it::setAppLayoutVerticalArrangement }
    )

    val appLayoutHorizontalArrangement = SettingsField(
        flow = settingsFlow.map { it.appLayoutHorizontalArrangement },
        setter = { it::setAppLayoutHorizontalArrangement }
    )

    val appLayoutHorizontalPadding = SettingsField(
        flow = settingsFlow.map { it.appLayoutHorizontalPadding },
        setter = { it::setAppLayoutHorizontalPadding }
    )

    val appLayoutVerticalPadding = SettingsField(
        flow = settingsFlow.map { it.appLayoutVerticalPadding },
        setter = { it::setAppLayoutVerticalPadding }
    )

    val appLayoutSortBy = SettingsField(
        flow = settingsFlow.map { it.appLayoutSortBy },
        setter = { it::setAppLayoutSortBy }
    )

    val appCardRemoveSpaces = SettingsField(
        flow = settingsFlow.map { it.appCardLabelRemoveSpaces },
        setter = { it::setAppCardLabelRemoveSpaces }
    )

    val appCardLabelLowercase = SettingsField(
        flow = settingsFlow.map { it.appCardLabelLowercase },
        setter = { it::setAppCardLabelLowercase }
    )

    val appCardTextColor = SettingsField(
        flow = settingsFlow.map { it.appCardTextColor },
        setter = { it::setAppCardTextColor }
    )

    val appCardPadding = SettingsField(
        flow = settingsFlow.map { it.appCardPadding },
        setter = { it::setAppCardPadding }
    )

    val appCardTextStyle = SettingsField(
        flow = settingsFlow.map { it.appCardTextStyle },
        setter = { it::setAppCardTextStyle }
    )

    val appCardFontFamily = SettingsField(
        flow = settingsFlow.map { it.appCardFontFamily },
        setter = { it::setAppCardFontFamily }
    )

    fun backupReset() = updateSettings { settingsRepo.getDefault() }

    fun setHiddenApps(packageNames: List<String>) {
        launch { appsRepo.setHiddenApps(packageNames) }
    }

    fun addSplitScreenShortcut(appTop: LauncherApp, appBottom: LauncherApp) {
        launch {
            appsRepo.addSplitScreenShortcut(
                appTop.packageName,
                appBottom.packageName
            )
        }
    }

    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        launch {
            appsRepo.removeSplitScreenShortcut(shortcut)
        }
    }
}
