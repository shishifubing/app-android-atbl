package com.shishifubing.atbl.domain

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherApps
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.content.pm.LauncherApps as LauncherAppsAndroid

class LauncherAppsRepository(
    private val dataStore: DataStore<LauncherApps>,
    private val parent: ComponentActivity
) {

    private val tag = LauncherAppsRepository::class.simpleName

    val appsFlow: Flow<LauncherApps> = dataStore.data.catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            Log.e(tag, "Error loading apps", exception)
            emit(LauncherApps.getDefaultInstance())
        } else {
            throw exception
        }
    }

    fun getAppIcon(packageName: String): ImageBitmap {
        return parent.packageManager.getApplicationIcon(packageName)
            .toBitmap(config = Bitmap.Config.ARGB_8888)
            .asImageBitmap()
    }

    suspend fun updateIsHomeApp(isHomeApp: Boolean) {
        update { it.isHomeApp = isHomeApp }
    }

    suspend fun toggleIsHidden(packageName: String) {
        update {
            it.putApps(
                packageName,
                it.getAppsOrThrow(packageName).toBuilder().also { app ->
                    app.isHidden = !app.isHidden
                }.build()
            )
        }
    }

    suspend fun removeApp(packageName: String) {
        update { it.removeApps(packageName) }
    }

    suspend fun addSplitScreenShortcut(appTop: String, appBottom: String) {
        update { current ->
            current.addSplitScreenShortcuts(
                LauncherSplitScreenShortcut.getDefaultInstance()
                    .toBuilder()
                    .setAppBottom(current.getAppsOrThrow(appBottom))
                    .setAppTop(current.getAppsOrThrow(appTop))
                    .build()
            )
        }
    }

    suspend fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        update { current ->
            current.removeSplitScreenShortcuts(
                current.splitScreenShortcutsList.indexOf(shortcut)
            )
        }
    }

    suspend fun reloadApp(packageName: String) {
        update { it.putApps(packageName, fetchApp(packageName)) }
    }

    suspend fun setHiddenApps(packageNames: List<String>) {
        update { current ->
            current.appsMap.forEach { (name, app) ->
                current.putApps(
                    name,
                    app.toBuilder()
                        .setIsHidden(packageNames.contains(name))
                        .build()
                )
            }
        }
    }

    suspend fun fetchInitial(): LauncherApps {
        return dataStore.data.first().let {
            if (it.appsMap.isEmpty()) {
                reloadApps()
            } else {
                it
            }
        }
    }

    private suspend fun reloadApps(): LauncherApps {
        return update { current ->
            current.clearApps().putAllApps(
                fetchAllApps().map { (name, app) ->
                    val isHidden = current.getAppsOrDefault(name, app).isHidden
                    name to app.toBuilder().setIsHidden(isHidden).build()
                }.toMap()
            )
        }
    }

    private suspend fun update(action: (LauncherApps.Builder) -> Unit): LauncherApps {
        return dataStore.updateData { it.toBuilder().also(action).build() }
    }

    private fun fetchAllApps(): Map<String, LauncherApp> {
        val queryResults = queryPackageManager(
            Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)
        )
        return queryResults.associate { info ->
            val activity = info.activityInfo
            val name = activity.packageName
            name to LauncherApp.newBuilder()
                .setLabel(activity.loadLabel(parent.packageManager).toString())
                .setPackageName(name)
                .addAllShortcuts(getShortcuts(name))
                .build()
        }
    }

    private fun fetchApp(packageName: String): LauncherApp {
        val queryResults = queryPackageManager(
            Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(packageName)
        )
        if (queryResults.isEmpty()) {
            throw IllegalArgumentException(
                "could not fetch $packageName - empty queryResults"
            )
        }
        val info = queryResults[0].activityInfo
        return LauncherApp.newBuilder()
            .setLabel(info.loadLabel(parent.packageManager).toString())
            .setPackageName(info.packageName)
            .addAllShortcuts(getShortcuts(info.packageName))
            .build()
    }

    private fun queryPackageManager(intent: Intent): List<ResolveInfo> {
        return when {
            Build.VERSION.SDK_INT >= 33 -> parent.packageManager
                .queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )

            else -> parent.packageManager.queryIntentActivities(
                intent, 0
            )
        }
    }

    private fun getShortcuts(packageName: String): List<LauncherAppShortcut> {
        val launcherAppsService = parent.getSystemService(
            LauncherAppsAndroid::class.java
        )
        val userHandle = android.os.Process.myUserHandle()
        val query = LauncherAppsAndroid.ShortcutQuery()
            .setPackage(packageName)
            .setQueryFlags(
                android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                        android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
            )
        val shortcuts = try {
            launcherAppsService.getShortcuts(query, userHandle) ?: listOf()
        } catch (e: SecurityException) {
            Log.d(tag, "got a security exception: $e")
            listOf()
        }
        return shortcuts.map { info ->
            val label = info.longLabel ?: info.shortLabel ?: info.`package`
            LauncherAppShortcut.newBuilder()
                .setShortcutId(info.id)
                .setPackageName(info.`package`)
                .setLabel(label.toString())
                .build()
        }
    }
}

object LauncherAppsSerializer : Serializer<LauncherApps> {
    override val defaultValue: LauncherApps = LauncherApps.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LauncherApps {
        try {
            return LauncherApps.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read app info.", exception)
        }
    }

    override suspend fun writeTo(t: LauncherApps, output: OutputStream) =
        t.writeTo(output)
}