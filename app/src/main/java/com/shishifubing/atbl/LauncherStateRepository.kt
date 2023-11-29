package com.shishifubing.atbl

import android.content.Context
import android.util.Log
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import java.io.InputStream
import java.io.OutputStream


private val tag = LauncherStateRepository::class.simpleName

private val handler = CoroutineExceptionHandler { ctx, exception ->
    Log.d(tag, "got repo exception: $exception")
}

private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + handler)


private val Context.dataStore by dataStore(
    fileName = "launcherApps.pb",
    serializer = LauncherStateSerializer,
    scope = scope
)


class LauncherStateRepository(
    private val manager: LauncherManager,
    private val context: Context
) {

    private val tag = LauncherStateRepository::class.simpleName

    companion object {
        val defaultSettings: LauncherSettings = LauncherSettings
            .getDefaultInstance()
            .toBuilder()
            .setAppCardPadding(0)
            .setAppCardLabelRemoveSpaces(true)
            .setAppCardTextStyle(LauncherTextStyle.HeadlineSmall)
            .setAppCardLabelLowercase(true)
            .setAppLayoutHorizontalArrangement(
                LauncherHorizontalArrangement.HorizontalStart
            )
            .setAppLayoutVerticalArrangement(
                LauncherVerticalArrangement.VerticalSpaceBetween
            )
            .setAppCardTextColor(LauncherTextColor.Unspecified)
            .setAppCardFontFamily(LauncherFontFamily.Monospace)
            .setAppLayoutSortBy(LauncherSortBy.SortByLabel)
            .setAppLayoutReverseOrder(false)
            .setAppLayoutHorizontalPadding(0)
            .setAppLayoutVerticalPadding(0)
            .setAppCardSplitScreenSeparator("/")
            .build()


        val defaultState: LauncherState = LauncherState
            .getDefaultInstance()
            .toBuilder()
            .setSettings(defaultSettings)
            .setIsHomeApp(false)
            .build()
    }

    fun observeState() = context.dataStore.data.catch { emit(defaultState) }

    private suspend fun update(action: LauncherState.Builder.() -> Unit) {
        context.dataStore.updateData { it.toBuilder().apply(action).build() }
    }

    private suspend fun updateScreen(
        screen: Int,
        action: LauncherScreen.Builder.(state: LauncherStateOrBuilder) -> Unit
    ) = update {
        val builder = this
        setScreens(
            screen,
            getScreens(screen).toBuilder().apply { action(builder) }
        )
    }

    private suspend fun addScreen(screen: LauncherScreen) = update {
        addScreens(screen)
    }

    suspend fun addEmptyScreen() = addScreen(
        LauncherScreen.getDefaultInstance()
    )

    suspend fun removeScreen(screen: Int) = update {
        removeScreens(screen)
    }

    private suspend fun updateApp(
        packageName: String,
        action: LauncherApp.Builder.() -> Unit
    ) = update {
        putApps(
            packageName,
            getAppsOrThrow(packageName).toBuilder().apply(action).build()
        )
    }

    private fun shortcutKey(shortcut: LauncherSplitScreenShortcutOrBuilder): String {
        return "${shortcut.appTop.packageName}/${shortcut.appBottom.packageName}"
    }

    suspend fun updateIsHomeApp() = update { isHomeApp = manager.isHomeApp() }

    suspend fun setIsHidden(packageName: String, isHidden: Boolean) =
        updateApp(packageName) {
            setIsHidden(isHidden)
        }

    suspend fun removeApp(packageName: String) = update {
        if (appsMap.containsKey(packageName)) {
            removeApps(packageName)
        }
    }

    suspend fun addSplitScreenShortcut(appTop: String, appBottom: String) =
        update {
            val shortcut = LauncherSplitScreenShortcut.getDefaultInstance()
                .toBuilder()
                .setAppBottom(getAppsOrThrow(appBottom))
                .setAppTop(getAppsOrThrow(appTop))
                .build()
            putSplitScreenShortcuts(shortcutKey(shortcut), shortcut)
        }

    suspend fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) =
        update { removeSplitScreenShortcuts(shortcutKey(shortcut)) }

    suspend fun reloadApp(packageName: String) = updateApp(packageName) {
        mergeFrom(manager.getApp(packageName))
    }

    suspend fun setHiddenApps(packageNames: List<String>) = update {
        appsMap.forEach { (packageName, app) ->
            val updatedApp = app.toBuilder()
                .setIsHidden(packageNames.contains(packageName))
                .build()
            putApps(app.packageName, updatedApp)
        }
    }

    suspend fun resetSettings() = updateSettings {
        clear().mergeFrom(defaultSettings)
    }

    suspend fun updateSettings(action: LauncherSettings.Builder.() -> Unit) =
        update {
            settings = settings.toBuilder().apply(action).build()
        }

    suspend fun reloadState() = update {
        val newApps = manager.fetchAllApps().map { (name, app) ->
            val isHidden = getAppsOrDefault(name, app).isHidden
            name to app.toBuilder().setIsHidden(isHidden).build()
        }.toMap()
        clearApps()
        putAllApps(newApps)
        if (screensCount == 0) {
            val itemApps = LauncherScreenItem.getDefaultInstance()
                .toBuilder()
                .setComplex(LauncherScreenItemComplex.APPS)
                .build()
            val itemShortcuts = LauncherScreenItem.getDefaultInstance()
                .toBuilder()
                .setComplex(LauncherScreenItemComplex.SPLIT_SCREEN_SHORTCUTS)
                .build()
            val screen = LauncherScreen.getDefaultInstance()
                .toBuilder()
                .addAllItems(listOf(itemApps, itemShortcuts))
            addScreens(screen)
        }
        isHomeApp = manager.isHomeApp()
    }
}

private object LauncherStateSerializer : Serializer<LauncherState> {
    override val defaultValue = LauncherStateRepository.defaultState

    override suspend fun readFrom(input: InputStream): LauncherState {
        return try {
            LauncherState.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: LauncherState, output: OutputStream) =
        t.writeTo(output)
}