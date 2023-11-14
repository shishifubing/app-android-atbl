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
        update { current -> current.setIsHomeApp(isHomeApp) }
    }

    suspend fun hideApp(packageName: String) {
        update { current ->
            val index = getAppIndex(current, packageName)
            current.setApps(
                index,
                current.getApps(index).toBuilder().setIsHidden(true).build()
            )
        }
    }

    suspend fun removeApp(packageName: String) {
        update { current ->
            current.removeApps(getAppIndex(current, packageName))
        }
    }

    suspend fun addApp(packageName: String) {
        update { current -> current.addApps(fetchApp(packageName)) }
    }

    suspend fun addSplitScreenShortcut(appTop: String, appBottom: String) {
        update { current ->
            current.addSplitScreenShortcuts(
                LauncherSplitScreenShortcut.getDefaultInstance()
                    .toBuilder()
                    .setAppBottom(getApp(current, appBottom))
                    .setAppTop(getApp(current, appTop))
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

    suspend fun updateApp(packageName: String) {
        update { current ->
            current.setApps(
                getAppIndex(current, packageName),
                fetchApp(packageName)
            )
        }
    }

    suspend fun setHiddenApps(packageNames: List<String>) {
        update { current ->
            val hiddenApps = packageNames.toSet()
            val newApps = current.appsList.map { app ->
                app.toBuilder()
                    .setIsHidden(hiddenApps.contains(app.packageName))
                    .build()
            }
            current.clearApps().addAllApps(newApps)
        }
    }

    suspend fun fetchInitial(): LauncherApps {
        val apps = dataStore.data.first()
        return if (apps.appsList.isEmpty()) {
            reloadApps()
        } else {
            apps
        }
    }

    suspend fun reloadApps(): LauncherApps {
        return update { current ->
            val hidden = current.appsList
                .filter { it.isHidden }
                .map { it.packageName }
                .toSet()
            val newApps = fetchAllApps().map { app ->
                if (hidden.contains(app.packageName)) {
                    app.toBuilder().setIsHidden(true).build()
                } else {
                    app
                }
            }
            current.clearApps().addAllApps(newApps)
        }
    }

    private suspend fun update(
        action: (LauncherApps.Builder) -> (LauncherApps.Builder)
    ): LauncherApps {
        return dataStore.updateData { current -> action(current.toBuilder()).build() }
    }

    private fun getAppIndex(
        current: LauncherApps.Builder, packageName: String
    ): Int {
        val index =
            current.appsList.indexOfFirst { it.packageName == packageName }
        return if (index != -1) index else throw IllegalArgumentException(
            "could not get app index of $packageName, it is not in the datastore"
        )
    }

    private fun getApp(
        current: LauncherApps.Builder,
        packageName: String
    ): LauncherApp {
        return current.appsList.firstOrNull { it.packageName == packageName }
            ?: throw IllegalArgumentException(
                "could not get app $packageName, it is not in the datastore"
            )
    }

    private fun fetchAllApps(): List<LauncherApp> {
        val queryResults = queryPackageManager(
            Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)
        )
        return queryResults.map { info ->
            val label = info.activityInfo.loadLabel(parent.packageManager)
            LauncherApp.newBuilder()
                .setLabel(label.toString())
                .setPackageName(info.activityInfo.packageName)
                .addAllShortcuts(getShortcuts(info.activityInfo.packageName))
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
        val shortcuts = try {
            launcherAppsService.getShortcuts(
                LauncherAppsAndroid.ShortcutQuery()
                    .setPackage(packageName)
                    .setQueryFlags(
                        android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                                android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    ),
                userHandle
            ) ?: listOf()
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