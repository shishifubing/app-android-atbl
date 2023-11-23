package com.shishifubing.atbl.domain

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherState
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
        appTop: String, appBottom: String
    ) = update {
        val shortcut = LauncherSplitScreenShortcut.getDefaultInstance()
            .toBuilder()
            .setAppBottom(getAppsOrThrow(appBottom))
            .setAppTop(getAppsOrThrow(appTop))
            .build()
        addSplitScreenShortcuts(shortcut)
    }

    suspend fun removeSplitScreenShortcut(
        shortcut: LauncherSplitScreenShortcut
    ) = update {
        removeSplitScreenShortcuts(splitScreenShortcutsList.indexOf(shortcut))
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