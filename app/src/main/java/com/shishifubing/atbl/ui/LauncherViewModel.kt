package com.shishifubing.atbl.ui


import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherScreenItemComplex
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSettingsRepository
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
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


typealias LaunchShortcut = (shortcut: LauncherAppShortcut) -> Unit
typealias GetAppIcon = (packageName: String) -> ImageBitmap

class LauncherViewModel(
    settingsRepo: LauncherSettingsRepository,
    private val stateRepo: LauncherStateRepository,
    private val manager: LauncherManager
) : ViewModel() {
    companion object {
        val Factory = launcherViewModelFactory {
            LauncherViewModel(
                settingsRepo = settingsRepo!!,
                manager = appsManager!!,
                stateRepo = stateRepo!!
            )
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private val _showHiddenAppsFlow = MutableStateFlow(false)

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }

    val uiState = combine(
        settingsRepo.settingsFlow,
        stateRepo.stateFlow,
        _showHiddenAppsFlow
    ) { settings, state, showHiddenApps ->
        val apps = transformApps(
            state.appsMap.values, showHiddenApps, settings
        )
        val splitScreenShortcuts = state.splitScreenShortcutsMap.values
            .sortedBy { it.appTop.packageName }
        val screens = state.screensList.map { screen ->
            LauncherScreenUiState(
                showHiddenApps = showHiddenApps,
                appCardSettings = settings.appCardSettings(),
                launcherRowSettings = settings.rowSettings(),
                isHomeApp = state.isHomeApp,
                items = screen.itemsList.mapNotNull {
                    when (it.complex) {
                        LauncherScreenItemComplex.APPS ->
                            LauncherUiItem.Apps(apps)

                        LauncherScreenItemComplex.SPLIT_SCREEN_SHORTCUTS
                        -> LauncherUiItem.Shortcuts(splitScreenShortcuts)

                        else -> null
                    }
                },
            )
        }
        LauncherUiState.Success(screens = screens)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState.Loading
    )

    val splitScreenShortcutActions = object : SplitScreenShortcutActions {
        override fun launchSplitScreenShortcut(
            shortcut: LauncherSplitScreenShortcut
        ) = manager.launchSplitScreen(shortcut)

        override fun removeSplitScreenShortcut(
            shortcut: LauncherSplitScreenShortcut
        ) = stateAction {
            removeSplitScreenShortcut(shortcut)
        }
    }

    val appActions = object : AppActions {
        override fun launchAppUninstall(
            packageName: String
        ) = manager.launchAppUninstall(packageName)

        override fun setIsHidden(
            packageName: String, isHidden: Boolean
        ) = stateAction {
            setIsHidden(packageName, isHidden)
        }

        override fun launchAppInfo(
            packageName: String
        ) = manager.launchAppInfo(packageName)

        override fun launchApp(
            packageName: String
        ) = manager.launchApp(packageName)

        override fun launchShortcut(
            shortcut: LauncherAppShortcut
        ) = manager.launchAppShortcut(shortcut)

        override fun showHiddenAppsToggle() {
            _showHiddenAppsFlow.value = _showHiddenAppsFlow.value.not()
        }

        override fun getAppIcon(
            packageName: String
        ): ImageBitmap = manager.getAppIcon(packageName)

        override fun transformLabel(
            label: String,
            settings: LauncherAppCardSettings
        ): String = label
            .let { if (settings.removeSpaces) it.replace(" ", "") else it }
            .let { if (settings.lowercase) it.lowercase() else it }
    }

    fun stateAction(
        action: suspend LauncherStateRepository.() -> Unit
    ) = launch { action.invoke(stateRepo) }

    private fun transformApps(
        apps: MutableCollection<LauncherApp>,
        showHiddenApps: Boolean,
        settings: LauncherSettings
    ): List<LauncherApp> = apps
        .let {
            when (settings.appLayoutSortBy) {
                LauncherSortBy.SortByLabel -> it.sortedBy { app -> app.label }
                else -> it.sortedBy { app -> app.label }
            }
        }
        .let {
            if (settings.appLayoutReverseOrder) it.reversed() else it
        }
        .let {
            if (showHiddenApps) it else it.filterNot { app -> app.isHidden }
        }
}


sealed interface LauncherUiState {
    data class Success(
        val screens: List<LauncherScreenUiState>,
    ) : LauncherUiState

    object Loading : LauncherUiState
}

sealed interface LauncherUiItem {
    data class Apps(val apps: List<LauncherApp>) : LauncherUiItem
    data class Shortcuts(
        val shortcuts: List<LauncherSplitScreenShortcut>
    ) : LauncherUiItem
}

interface AppActions {
    fun launchAppUninstall(packageName: String)
    fun setIsHidden(packageName: String, isHidden: Boolean)
    fun launchAppInfo(packageName: String)
    fun launchApp(packageName: String)
    fun launchShortcut(shortcut: LauncherAppShortcut)
    fun showHiddenAppsToggle()
    fun getAppIcon(packageName: String): ImageBitmap
    fun transformLabel(
        label: String,
        settings: LauncherAppCardSettings
    ): String
}

interface SplitScreenShortcutActions {
    fun launchSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut)
    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut)
}

data class LauncherScreenUiState(
    val items: List<LauncherUiItem>,
    val showHiddenApps: Boolean,
    val isHomeApp: Boolean,
    val appCardSettings: LauncherAppCardSettings,
    val launcherRowSettings: LauncherRowSettings
)

data class LauncherAppCardSettings(
    val removeSpaces: Boolean,
    val lowercase: Boolean,
    val padding: Int,
    val textStyle: LauncherTextStyle,
    val fontFamily: LauncherFontFamily,
    val textColor: LauncherTextColor,
    val shortcutSeparator: String
)

data class LauncherRowSettings(
    val horizontalPadding: Int,
    val verticalPadding: Int,
    val horizontalArrangement: LauncherHorizontalArrangement,
    val verticalArrangement: LauncherVerticalArrangement
)

private fun LauncherSettings.rowSettings() = LauncherRowSettings(
    horizontalPadding = appLayoutHorizontalPadding,
    verticalPadding = appLayoutVerticalPadding,
    horizontalArrangement = appLayoutHorizontalArrangement,
    verticalArrangement = appLayoutVerticalArrangement
)

private fun LauncherSettings.appCardSettings() = LauncherAppCardSettings(
    removeSpaces = appCardLabelRemoveSpaces,
    lowercase = appCardLabelLowercase,
    padding = appCardPadding,
    shortcutSeparator = appCardSplitScreenSeparator,
    textStyle = appCardTextStyle,
    fontFamily = appCardFontFamily,
    textColor = appCardTextColor
)

private fun LauncherAppCardSettings.transformLabel(label: String) = label
    .let { if (removeSpaces) it.replace(" ", "") else it }
    .let {
        if (lowercase) it.lowercase() else it
    }