package com.shishifubing.atbl.ui


import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Screen
import com.shishifubing.atbl.Model.Settings
import com.shishifubing.atbl.Model.Settings.AppCard
import com.shishifubing.atbl.Model.SplitScreenShortcut
import com.shishifubing.atbl.Model.SplitScreenShortcuts
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val tag = HomeState::class.java.name

@Immutable
sealed interface HomeState {
    @Immutable
    data class Success(
        val items: List<RowItems>,
        val settings: Settings,
        val showHiddenApps: Boolean,
        val isHomeApp: Boolean,
        val appShortcutButtons: Map<String, HomeDialogButtons>
    ) : HomeState

    @Immutable
    data object Loading : HomeState

    @Immutable
    data class RowItems(val items: List<RowItem>)

    @Immutable
    sealed interface RowItem {
        val label: String

        @Immutable
        data class App(
            val app: Model.App,
            override val label: String
        ) : RowItem

        @Immutable
        data class SplitScreenShortcut(
            val shortcut: Model.SplitScreenShortcut,
            override val label: String
        ) : RowItem
    }
}

class HomeViewModel(
    private val stateRepo: LauncherStateRepository,
    private val manager: LauncherManager
) : ViewModel() {
    companion object {
        val Factory = launcherViewModelFactory {
            HomeViewModel(
                manager = manager,
                stateRepo = stateRepo
            )
        }
    }

    private val _errorFlow = MutableStateFlow<Throwable?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _errorFlow.update { e }
    }

    private var prevState = Defaults.State

    val uiState = stateRepo.observeState().map { stateResult ->
        val state = stateResult.fold(
            onSuccess = {
                prevState = it
                it
            },
            onFailure = {
                _errorFlow.update { it }
                prevState
            }
        )
        HomeState.Success(
            settings = state.settings,
            showHiddenApps = state.showHiddenApps,
            isHomeApp = state.isHomeApp,
            items = state.screensList.map { screen ->
                screenToHomeRowItems(screen, state)
            },
            appShortcutButtons = appShortcutsToDialogButtons(apps = state.apps)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeState.Loading
    )

    private fun appShortcutsToDialogButtons(apps: Model.Apps): Map<String, HomeDialogButtons> {
        return apps.appsMap
            .map { (packageName, app) ->
                packageName to HomeDialogButtons(
                    buttons = app.shortcutsList.map { shortcut ->
                        HomeDialogButton(
                            label = shortcut.label,
                            onClick = { launchShortcut(shortcut) }
                        )
                    }
                )
            }
            .toMap()
    }

    private fun screenToHomeRowItems(
        screen: Screen,
        state: Model.State
    ): HomeState.RowItems {
        val items = screen.itemsList
            .flatMap { itemModel ->
                itemToHomeRowItems(
                    apps = state.apps,
                    splitScreenShortcuts = state.splitScreenShortcuts,
                    item = itemModel,
                    settings = state.settings.appCard
                )
            }
            .let { items ->
                when (val sort = state.settings.layout.sortBy) {
                    Settings.SortBy.SortByLabel -> items.sortedBy { it.label }

                    else -> throw NotImplementedError("not implemented sort - $sort")
                }
            }
            .let { items ->
                if (state.settings.layout.reverseOrder) {
                    items.reversed()
                } else {
                    items
                }
            }
        return HomeState.RowItems(items = items)
    }

    private fun itemToHomeRowItems(
        apps: Model.Apps,
        splitScreenShortcuts: SplitScreenShortcuts,
        item: Model.ScreenItem,
        settings: AppCard
    ): List<HomeState.RowItem> {
        return when (item.itemCase) {
            Model.ScreenItem.ItemCase.APP -> {
                val model = apps.getAppsOrThrow(item.app.packageName)
                listOf(
                    HomeState.RowItem.App(
                        app = model,
                        label = transformLabel(model, settings)
                    )
                )
            }

            Model.ScreenItem.ItemCase.APPS -> {
                apps.appsMap.values.map { app ->
                    HomeState.RowItem.App(
                        app = app,
                        label = transformLabel(app, settings)
                    )
                }
            }

            Model.ScreenItem.ItemCase.SHORTCUT -> {
                val model =
                    splitScreenShortcuts.getShortcutsOrThrow(item.shortcut.key)
                listOf(
                    HomeState.RowItem.SplitScreenShortcut(
                        shortcut = model,
                        label = transformLabel(model, settings)
                    )
                )
            }

            Model.ScreenItem.ItemCase.SHORTCUTS -> {
                splitScreenShortcuts.shortcutsMap.map {
                    HomeState.RowItem.SplitScreenShortcut(
                        shortcut = it.value,
                        label = transformLabel(it.value, settings)
                    )
                }
            }

            Model.ScreenItem.ItemCase.ITEM_NOT_SET -> {
                Log.d(tag, "item is not set for some reason?")
                listOf()
            }

            else -> {
                throw NotImplementedError("not implemented item: $item")
            }
        }
    }

    private fun transformLabel(app: Model.App, settings: AppCard): String {
        return transformLabel(app.label, settings)
    }

    private fun transformLabel(
        shortcut: SplitScreenShortcut,
        settings: AppCard
    ): String {
        return transformLabel(
            shortcut.appSecond.label + settings.splitScreenSeparator + shortcut.appFirst.label,
            settings
        )
    }

    private fun transformLabel(
        label: String,
        settings: AppCard
    ): String {
        return label
            .let {
                if (settings.labelRemoveSpaces) {
                    it.replace(" ", "")
                } else {
                    it
                }
            }
            .let { if (settings.labelLowercase) it.lowercase() else it }
    }

    fun launchSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        managerAction {
            launchSplitScreen(
                shortcut.appFirst.packageName,
                shortcut.appSecond.packageName
            )
        }
    }

    fun removeSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut.key) }
    }

    fun launchAppUninstall(app: Model.App) {
        managerAction { this.launchAppUninstall(app.packageName) }
    }

    fun setIsHidden(app: Model.App, isHidden: Boolean) {
        stateAction { setIsHidden(app.packageName, isHidden) }
    }

    fun launchAppInfo(app: Model.App) {
        managerAction { this.launchAppInfo(app.packageName) }
    }

    fun launchApp(app: Model.App) {
        managerAction { this.launchApp(app.packageName) }
    }

    private fun launchShortcut(shortcut: Model.AppShortcut) {
        managerAction { this.launchAppShortcut(shortcut) }
    }

    fun setShowHiddenApps(showHiddenApps: Boolean) {
        stateAction { this.setShowHiddenApps(showHiddenApps) }
    }

    fun addScreenAfter(screen: Int) {
        stateAction { this.addScreenAfter(screen) }
    }

    fun addScreenBefore(screen: Int) {
        stateAction { this.addScreenBefore(screen) }
    }

    fun removeScreen(screen: Int) {
        stateAction { this.removeScreen(screen) }
    }

    private fun managerAction(action: suspend LauncherManager.() -> Unit) {
        launch { action.invoke(manager) }
    }

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }
}



