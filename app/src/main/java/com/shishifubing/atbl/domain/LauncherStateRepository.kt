package com.shishifubing.atbl.domain

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.shishifubing.atbl.LauncherScreen
import com.shishifubing.atbl.LauncherScreenItem
import com.shishifubing.atbl.LauncherScreenItemComplex
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherState
import com.shishifubing.atbl.LauncherStateOrBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class LauncherStateRepository(
    private val dataStore: DataStore<LauncherState>,
    private val manager: LauncherAppsManager
) {

    private val tag = LauncherStateRepository::class.simpleName

    val stateFlow: Flow<Result<LauncherState>> = dataStore.data
        .map { Result.success(it) }
        .catch { Result.failure<LauncherState>(it) }

    private suspend fun update(action: LauncherState.Builder.() -> Unit): LauncherState {
        return dataStore.updateData { it.toBuilder().apply(action).build() }
    }

    private suspend fun updateScreen(
        screen: Int,
        action: LauncherScreen.Builder.(state: LauncherStateOrBuilder) -> Unit
    ) {
        update {
            val builder = this
            setScreens(
                screen,
                getScreens(screen).toBuilder().apply { action(builder) })
        }
    }

    suspend fun updateIsHomeApp() = update { isHomeApp = manager.isHomeApp() }

    suspend fun setIsHidden(packageName: String, isHidden: Boolean) = update {
        val updatedApp = getAppsOrThrow(packageName).toBuilder()
            .setIsHidden(isHidden)
            .build()
        putApps(packageName, updatedApp)
    }

    suspend fun removeApp(packageName: String) = update {
        removeApps(packageName)
    }

    suspend fun addSplitScreenShortcut(
        screen: Int, appTop: String, appBottom: String
    ) = updateScreen(screen) { state ->
        val shortcut = LauncherSplitScreenShortcut.getDefaultInstance()
            .toBuilder()
            .setAppBottom(state.getAppsOrThrow(appBottom))
            .setAppTop(state.getAppsOrThrow(appTop))
        val screenItem = LauncherScreenItem.getDefaultInstance()
            .toBuilder()
            .setSplitScreenShortcut(shortcut)
        addItems(screenItem)
    }

    suspend fun removeSplitScreenShortcut(
        screen: Int, shortcut: LauncherSplitScreenShortcut
    ) = updateScreen(screen) {
        val index = itemsList.indexOfFirst {
            it.hasSplitScreenShortcut() && it.splitScreenShortcut == shortcut
        }
        if (index != -1) {
            removeItems(index)
        }
    }

    suspend fun reloadApp(packageName: String) = update {
        putApps(packageName, manager.getApp(packageName))
    }


    suspend fun setHiddenApps(packageNames: List<String>) = update {
        appsMap.forEach { (name, app) ->
            val updatedApp = app.toBuilder()
                .setIsHidden(packageNames.contains(name))
                .build()
            putApps(name, updatedApp)
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
            val item = LauncherScreenItem.getDefaultInstance()
                .toBuilder()
                .setComplex(LauncherScreenItemComplex.APPS)
            val screen = LauncherScreen.getDefaultInstance()
                .toBuilder()
                .addItems(item)
            addScreens(screen)
        }
        isHomeApp = manager.isHomeApp()
    }
}

object LauncherStateSerializer : Serializer<LauncherState> {
    override val defaultValue: LauncherState =
        LauncherState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LauncherState {
        try {
            return LauncherState.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read app info.", exception)
        }
    }

    override suspend fun writeTo(t: LauncherState, output: OutputStream) =
        t.writeTo(output)
}