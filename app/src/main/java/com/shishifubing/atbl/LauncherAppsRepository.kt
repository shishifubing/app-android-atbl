package com.shishifubing.atbl

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.content.pm.LauncherApps as LauncherAppsAndroid

class LauncherAppsRepository(
    private val dataStore: DataStore<LauncherApps>,
    private val context: Context
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

    fun getAppIcon(packageName: String): Drawable {
        return context.packageManager.getApplicationIcon(packageName)
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

    private suspend fun reloadApps(): LauncherApps {
        return update { current ->
            val hidden = current.appsList
                .filter { it.isHidden }
                .map { it.packageName }
                .toSet()
            current.clearApps().addAllApps(
                fetchAllApps().map { app ->
                    if (hidden.contains(app.packageName)) {
                        app.toBuilder().setIsHidden(true).build()
                    } else {
                        app
                    }
                }
            )
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
        return current.appsList.indexOfFirst { it.packageName == packageName }
    }

    private fun fetchAllApps(): List<LauncherApp> {
        val queryResults = queryPackageManager(
            Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)
        )
        return queryResults.map { info ->
            val label = info.activityInfo.loadLabel(context.packageManager)
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
        val info = queryResults[0].activityInfo
        val label = info.loadLabel(context.packageManager)
        return LauncherApp.newBuilder()
            .setLabel(label.toString())
            .setPackageName(info.packageName)
            .addAllShortcuts(getShortcuts(info.packageName))
            .build()
    }

    private fun queryPackageManager(intent: Intent): List<ResolveInfo> {
        return when {
            Build.VERSION.SDK_INT >= 33 -> context.packageManager
                .queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )

            else -> context.packageManager.queryIntentActivities(
                intent, 0
            )
        }
    }

    private fun getShortcuts(packageName: String): List<LauncherAppShortcut> {
        val launcherAppsService = context.getSystemService(
            LauncherAppsAndroid::class
                .java
        )
        val userHandle = android.os.Process.myUserHandle()
        return arrayOf(
            LauncherAppsAndroid.ShortcutQuery.FLAG_MATCH_MANIFEST,
            LauncherAppsAndroid.ShortcutQuery.FLAG_MATCH_PINNED,
            LauncherAppsAndroid.ShortcutQuery.FLAG_MATCH_DYNAMIC
        ).map { flags ->
            val shortcuts = try {
                launcherAppsService.getShortcuts(
                    LauncherAppsAndroid.ShortcutQuery()
                        .setPackage(packageName)
                        .setQueryFlags(flags),
                    userHandle
                ) ?: listOf()
            } catch (e: SecurityException) {
                Log.d(tag, "got a security exception: $e")
                listOf()
            }
            shortcuts.map { info ->
                val label = info.longLabel ?: info.shortLabel ?: info.`package`
                LauncherAppShortcut.newBuilder()
                    .setShortcutId(info.id)
                    .setPackageName(info.`package`)
                    .setLabel(label.toString())
                    .build()
            }
        }.flatten()
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