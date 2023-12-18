package com.shishifubing.atbl

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

private val tag = LauncherStateRepository::class.simpleName

class LauncherStateRepository(private val dataStore: DataStore<Model.State>) {

    private val tag = LauncherStateRepository::class.simpleName

    fun observeState(): Flow<Result<Model.State>> {
        return dataStore.data
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }

    }

    suspend fun stateOrThrow(): Model.State {
        return observeState().first().getOrThrow()
    }

    private suspend fun update(action: Model.State.Builder.() -> Unit): Model.State {
        return dataStore.updateData { it.toBuilder().apply(action).build() }
    }

    suspend fun updateSettings(action: Model.Settings.Builder.() -> Unit): Model.Settings {
        val newState = update {
            settings = settings.toBuilder().apply(action).build()
        }
        return newState.settings
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

    suspend fun updateIsHomeApp(value: Boolean) {
        update { isHomeApp = value }
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


    suspend fun setShowHiddenApps(showHiddenApps: Boolean) {
        update { this.showHiddenApps = showHiddenApps }
    }

    suspend fun reloadApp(app: Model.App) {
        updateApp(app.packageName) { mergeFrom(app) }
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

    suspend fun resetSettings(): Model.Settings {
        return updateSettings { mergeFrom(Defaults.Settings) }
    }

    suspend fun updateSettingsFromInputStream(stream: InputStream): Model.Settings {
        return updateSettings { mergeFrom(stream) }
    }


    suspend fun writeSettingsToOutputStream(stream: OutputStream): Model.Settings {
        val state = dataStore.data.first()
        state.settings.writeTo(stream)
        return state.settings
    }

    suspend fun reloadState(apps: Model.Apps, isHomeApp: Boolean) {
        update {
            val newApps = apps.appsMap.map { (name, app) ->
                val isHidden = this.apps.getAppsOrDefault(name, app).isHidden
                name to app.toBuilder().setIsHidden(isHidden).build()
            }.toMap()
            clearApps()
            setApps(Model.Apps.newBuilder().putAllApps(newApps))
            this.isHomeApp = isHomeApp
        }
    }
}

object LauncherStateSerializer : Serializer<Model.State> {
    override val defaultValue = Defaults.State

    override suspend fun readFrom(input: InputStream): Model.State {
        try {
            return Model.State.parseFrom(input)
        } catch (e: Throwable) {
            throw CorruptionException("Could not parse input stream", e)
        }
    }

    override suspend fun writeTo(t: Model.State, output: OutputStream) {
        try {
            t.writeTo(output)
        } catch (e: Throwable) {
            throw CorruptionException("Could write to output", e)
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

    val State: Model.State = Model.State
        .getDefaultInstance()
        .toBuilder()
        .setSettings(Settings)
        .setIsHomeApp(false)
        .setShowHiddenApps(false)
        .build()
}