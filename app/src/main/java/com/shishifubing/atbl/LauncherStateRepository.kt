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
import kotlinx.coroutines.flow.Flow
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

    fun observeState(): Flow<Model.State> {
        return context.dataStore.data.catch { emit(Defaults.State) }
    }

    private suspend fun update(action: Model.State.Builder.() -> Unit) {
        context.dataStore.updateData { it.toBuilder().apply(action).build() }
    }

    private suspend fun updateScreen(
        screen: Int,
        action: Model.State.Builder.(state: Model.StateOrBuilder) -> Unit
    ) {
        update {
            val builder = this
            setScreens(
                screen,
                getScreens(screen).toBuilder().apply { action(builder) }
            )
        }
    }

    private suspend fun addScreen(screen: Model.Screen) {
        update { addScreens(screen) }
    }

    suspend fun addEmptyScreen() {
        addScreen(Model.Screen.getDefaultInstance())
    }

    suspend fun removeScreen(screen: Int) {
        update { removeScreens(screen) }
    }

    private suspend fun updateApp(
        packageName: String,
        action: Model.App.Builder.() -> Unit
    ) {
        update {
            val newApp = apps.getAppsOrThrow(packageName)
                .toBuilder()
                .apply(action)
                .build()
            setApps(apps.toBuilder().putApps(packageName, newApp))
        }
    }

    suspend fun updateIsHomeApp() {
        update { isHomeApp = manager.isHomeApp() }
    }

    suspend fun setIsHidden(packageName: String, isHidden: Boolean) {
        updateApp(packageName) { setIsHidden(isHidden) }
    }

    suspend fun removeApp(packageName: String) {
        update {
            if (apps.appsMap.containsKey(packageName)) {
                setApps(apps.toBuilder().removeApps(packageName))
            }
        }
    }

    suspend fun addSplitScreenShortcut(firstApp: String, secondApp: String) {
        update {
            val newShortcut = Model.SplitScreenShortcut.getDefaultInstance()
                .toBuilder()
                .setAppBottom(apps.getAppsOrThrow(secondApp))
                .setAppTop(apps.getAppsOrThrow(firstApp))
                .setKey("$firstApp/$secondApp")
                .build()
            val newShortcuts = splitScreenShortcuts
                .toBuilder()
                .putShortcuts(newShortcut.key, newShortcut)
                .build()
            splitScreenShortcuts = newShortcuts
        }
    }

    suspend fun removeSplitScreenShortcut(shortcut: String) {
        update {
            val newShortcuts = splitScreenShortcuts.toBuilder()
                .removeShortcuts(shortcut)
                .build()
            splitScreenShortcuts = newShortcuts
        }
    }

    suspend fun reloadApp(packageName: String) {
        updateApp(packageName) { mergeFrom(manager.getApp(packageName)) }
    }

    suspend fun setHiddenApps(packageNames: List<String>) {
        update {
            val newApps = apps.appsMap.map { (packageName, app) ->
                packageName to app.toBuilder()
                    .setIsHidden(packageNames.contains(packageName))
                    .build()
            }.toMap()
            setApps(apps.toBuilder().putAllApps(newApps))
        }
    }

    suspend fun resetSettings() {
        update {
            settings = settings
                .toBuilder()
                .mergeFrom(Defaults.Settings)
                .build()
        }
    }

    suspend fun reloadState() {
        update {
            val newApps = manager.fetchAllApps().appsMap.map { (name, app) ->
                val isHidden = apps.getAppsOrDefault(name, app).isHidden
                name to app.toBuilder().setIsHidden(isHidden).build()
            }.toMap()
            clearApps()
            setApps(Model.Apps.newBuilder().putAllApps(newApps))
            if (screensCount == 0) {
                addScreens(Defaults.Screen)
            }
            isHomeApp = manager.isHomeApp()
        }
    }
}

object Defaults {
    val AppCardSettings: Model.Settings.AppCard = Model.Settings.AppCard
        .newBuilder()
        .setPadding(0)
        .setLabelRemoveSpaces(true)
        .setTextStyle(Model.Settings.TextStyle.HeadlineSmall)
        .setLabelLowercase(true)
        .setTextColor(Model.Settings.TextColor.Unspecified)
        .setFontFamily(Model.Settings.FontFamily.Monospace)
        .setSplitScreenSeparator("/")
        .build()

    val LayoutSettings: Model.Settings.Layout = Model.Settings.Layout
        .newBuilder()
        .setHorizontalArrangement(Model.Settings.HorizontalArrangement.HorizontalStart)
        .setVerticalArrangement(Model.Settings.VerticalArrangement.VerticalSpaceBetween)
        .setSortBy(Model.Settings.SortBy.SortByLabel)
        .setReverseOrder(false)
        .setHorizontalPadding(0)
        .setVerticalPadding(0)
        .build()

    val Settings: Model.Settings = Model.Settings
        .newBuilder()
        .setAppCard(AppCardSettings)
        .setLayout(LayoutSettings)
        .build()

    val Screen: Model.Screen = Model.Screen
        .newBuilder()
        .addItems(
            Model.ScreenItem
                .newBuilder()
                .setComplex(Model.ItemComplex.APPS)
                .build()
        )
        .addItems(
            Model.ScreenItem
                .newBuilder()
                .setComplex(Model.ItemComplex.SPLIT_SCREEN_SHORTCUTS)
                .build()
        )
        .build()

    val State: Model.State = Model.State
        .getDefaultInstance()
        .toBuilder()
        .setSettings(Settings)
        .setIsHomeApp(false)
        .build()
}

object LauncherStateSerializer : Serializer<Model.State> {
    override val defaultValue = Defaults.State

    override suspend fun readFrom(input: InputStream): Model.State {
        return try {
            Model.State.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: Model.State, output: OutputStream) {
        t.writeTo(output)
    }
}