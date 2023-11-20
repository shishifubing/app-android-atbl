package com.shishifubing.atbl.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LauncherViewModel(
    settingsRepo: LauncherSettingsRepository,
    private val appsRepo: LauncherAppsRepository,
    private val appsManager: LauncherAppsManager
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LauncherApplication
                LauncherViewModel(
                    settingsRepo = app.settingsRepo!!,
                    appsManager = app.appsManager!!,
                    appsRepo = app.appsRepo!!
                )
            }
        }
    }

    inner class SettingsField<T>(private val flow: Flow<T>) {
        @Composable
        fun collectAsState(): State<T> {
            return flow.collectAsState(runBlocking { flow.first() })
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private val settings = settingsRepo.settingsFlow
    private val _showHiddenApps = MutableStateFlow(false)
    val showHiddenAppsFlow = _showHiddenApps.asStateFlow()
    val shortcutsFlow = appsRepo.appsFlow.map { it.splitScreenShortcutsList }
    val appsFlow = appsRepo.appsFlow
        .combine(settings) { apps, settings ->
            apps.appsMap.values
                .let {
                    when (settings.appLayoutSortBy) {
                        LauncherSortBy.SortByLabel -> it.sortedBy { app -> app.label }
                        else -> it.sortedBy { app -> app.label }
                    }
                }
                .let {
                    if (settings.appLayoutReverseOrder) it.reversed() else it
                }
        }.combine(showHiddenAppsFlow) { apps, doShow ->
            if (doShow) {
                apps
            } else {
                apps.filterNot { it.isHidden }
            }
        }

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }
    val appCardSettings = SettingsField(settings.map {
        AppCardSettings(
            removeSpaces = it.appCardLabelRemoveSpaces,
            lowercase = it.appCardLabelLowercase,
            padding = it.appCardPadding,
            textStyle = it.appCardTextStyle,
            fontFamily = it.appCardFontFamily,
            textColor = it.appCardTextColor
        )
    })

    val launcherRowSettings = SettingsField(settings.map {
        LauncherRowSettings(
            horizontalPadding = it.appLayoutHorizontalPadding,
            verticalPadding = it.appLayoutVerticalPadding,
            horizontalArrangement = it.appLayoutHorizontalArrangement,
            verticalArrangement = it.appLayoutVerticalArrangement
        )
    })

    val shortcutSeparator = SettingsField(
        flow = settings.map { it.appCardSplitScreenSeparator }
    )

    val isHomeApp = SettingsField(appsRepo.appsFlow.map { it.isHomeApp })

    fun showHiddenAppsToggle() {
        _showHiddenApps.value = _showHiddenApps.value.not()
    }

    fun launchShortcut(shortcut: LauncherAppShortcut) {
        appsManager.launchAppShortcut(shortcut)
    }

    val appActions = AppActions(
        launchAppUninstall = { appsManager.launchAppUninstall(it) },
        getAppIcon = { appsRepo.getAppIcon(it) },
        launchAppInfo = { appsManager.launchAppInfo(it) },
        launchApp = { appsManager.launchApp(it) },
        toggleIsHidden = { launch { appsRepo.toggleIsHidden(it) } }
    )

    val splitScreenShortcutActions = SplitScreenShortcutActions(
        launch = { appsManager.launchSplitScreen(it) },
        remove = { launch { appsRepo.removeSplitScreenShortcut(it) } }
    )
}

data class AppCardSettings(
    val removeSpaces: Boolean,
    val lowercase: Boolean,
    val padding: Int,
    val textStyle: LauncherTextStyle,
    val fontFamily: LauncherFontFamily,
    val textColor: LauncherTextColor
) {
    fun transformLabel(label: String): String {
        return label.let {
            if (removeSpaces) {
                it.replace(" ", "")
            } else {
                it
            }
        }.let {
            if (lowercase) {
                it.lowercase()
            } else {
                it
            }
        }
    }
}

data class AppActions(
    val launchAppUninstall: (packageName: String) -> Unit,
    val toggleIsHidden: (packageName: String) -> Unit,
    val launchAppInfo: (packageName: String) -> Unit,
    val launchApp: (packageName: String) -> Unit,
    val getAppIcon: (packageName: String) -> ImageBitmap
)

data class SplitScreenShortcutActions(
    val launch: (shortcut: LauncherSplitScreenShortcut) -> Unit,
    val remove: (shortcut: LauncherSplitScreenShortcut) -> Unit,
)

data class LauncherRowSettings(
    val horizontalPadding: Int,
    val verticalPadding: Int,
    val horizontalArrangement: LauncherHorizontalArrangement,
    val verticalArrangement: LauncherVerticalArrangement
)