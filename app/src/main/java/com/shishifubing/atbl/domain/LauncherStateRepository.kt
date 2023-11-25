package com.shishifubing.atbl.domain

import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherScreen
import com.shishifubing.atbl.LauncherScreenItem
import com.shishifubing.atbl.LauncherScreenItemComplex
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherSplitScreenShortcutOrBuilder
import com.shishifubing.atbl.LauncherState
import com.shishifubing.atbl.LauncherStateOrBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.InputStream
import java.io.OutputStream

class LauncherStateRepository(
    private val dataStore: DataStore<LauncherState>,
    private val manager: LauncherAppsManager
) {

    private val tag = LauncherStateRepository::class.simpleName

    val stateFlow: Flow<LauncherState> = dataStore.data.catch {
        emit(LauncherStateSerializer.defaultValue)
    }

    private suspend fun update(action: LauncherState.Builder.() -> Unit) {
        dataStore.updateData { it.toBuilder().apply(action).build() }
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
        removeApps(packageName)
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

    suspend fun updateState() = update {
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

object LauncherStateSerializer : Serializer<LauncherState> {
    override val defaultValue: LauncherState =
        LauncherState.getDefaultInstance()

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