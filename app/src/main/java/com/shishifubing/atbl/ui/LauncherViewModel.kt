package com.shishifubing.atbl.ui


import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherScreenItemComplex
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import com.shishifubing.atbl.domain.LauncherStateRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LauncherViewModel(
    settingsRepo: LauncherSettingsRepository,
    private val stateRepo: LauncherStateRepository,
    private val appsManager: LauncherAppsManager
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LauncherApplication
                LauncherViewModel(
                    settingsRepo = app.settingsRepo!!,
                    appsManager = app.appsManager!!,
                    stateRepo = app.stateRepo!!
                )
            }
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
        val apps = state.appsMap.values
            .let {
                when (settings.appLayoutSortBy) {
                    LauncherSortBy.SortByLabel -> it.sortedBy { app -> app.label }
                    else -> it.sortedBy { app -> app.label }
                }
            }
            .let {
                if (settings.appLayoutReverseOrder) it.reversed() else it
            }
            .let { apps ->
                if (showHiddenApps) apps else apps.filterNot { it.isHidden }
            }
        val splitScreenShortcuts = state.splitScreenShortcutsMap.values
            .sortedBy { it.appTop.packageName }
        val appCardSettings = LauncherAppCardSettings(
            removeSpaces = settings.appCardLabelRemoveSpaces,
            lowercase = settings.appCardLabelLowercase,
            padding = settings.appCardPadding,
            shortcutSeparator = settings.appCardSplitScreenSeparator,
            textStyle = settings.appCardTextStyle,
            fontFamily = settings.appCardFontFamily,
            textColor = settings.appCardTextColor
        )
        val launcherRowSettings = LauncherRowSettings(
            horizontalPadding = settings.appLayoutHorizontalPadding,
            verticalPadding = settings.appLayoutVerticalPadding,
            horizontalArrangement = settings.appLayoutHorizontalArrangement,
            verticalArrangement = settings.appLayoutVerticalArrangement
        )
        val screens = state.screensList.map { screen ->
            LauncherScreenUiState(
                items = screen.itemsList.mapNotNull {
                    when (it.complex) {
                        LauncherScreenItemComplex.APPS ->
                            LauncherUiItem.Apps(apps)

                        LauncherScreenItemComplex.SPLIT_SCREEN_SHORTCUTS
                        -> LauncherUiItem.Shortcuts(splitScreenShortcuts)

                        else -> null
                    }
                },
                showHiddenApps = showHiddenApps,
                appCardSettings = appCardSettings,
                launcherRowSettings = launcherRowSettings,
                isHomeApp = state.isHomeApp
            )
        }
        LauncherUiState.Success(screens = screens)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState.Loading
    )

    val appActions = object : AppActions {
        override fun launchAppUninstall(packageName: String) {
            appsManager.launchAppUninstall(packageName)
        }

        override fun setIsHidden(packageName: String, isHidden: Boolean) {
            launch { stateRepo.setIsHidden(packageName, isHidden) }
        }

        override fun launchAppInfo(packageName: String) {
            appsManager.launchAppInfo(packageName)
        }

        override fun launchApp(packageName: String) {
            appsManager.launchApp(packageName)
        }

        override fun launchShortcut(shortcut: LauncherAppShortcut) {
            appsManager.launchAppShortcut(shortcut)
        }

        override fun showHiddenAppsToggle() {
            _showHiddenAppsFlow.value = _showHiddenAppsFlow.value.not()
        }

        override fun getAppIcon(packageName: String): ImageBitmap {
            return appsManager.getAppIcon(packageName)
        }

        override fun transformLabel(
            label: String,
            settings: LauncherAppCardSettings
        ): String {
            return label.let {
                if (settings.removeSpaces) it.replace(" ", "") else it
            }.let {
                if (settings.lowercase) it.lowercase() else it
            }
        }
    }

    val splitScreenShortcutActions = object : SplitScreenShortcutActions {
        override fun launchSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
            appsManager.launchSplitScreen(shortcut)
        }

        override fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
            launch { stateRepo.removeSplitScreenShortcut(shortcut) }
        }
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
