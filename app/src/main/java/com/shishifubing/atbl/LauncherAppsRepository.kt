package com.shishifubing.atbl

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    private suspend fun update(
        action: (LauncherApps.Builder) -> (LauncherApps.Builder)
    ): LauncherApps {
        return dataStore.updateData { current -> action(current.toBuilder()).build() }
    }

    suspend fun hideApp(packageName: String) {
        dataStore.updateData { current ->
            val curIndex =
                current.appsList.indexOfFirst { it.packageName == packageName }
            current.toBuilder().setApps(
                curIndex,
                current.getApps(curIndex).toBuilder().setIsHidden(true).build()
            ).build()
        }
    }

    suspend fun setHiddenApps(packageNames: List<String>) {
        dataStore.updateData { current ->
            val builder = current.toBuilder()
            val appsToShow = current.appsList
                .filter { it.isHidden }
                .map { it.packageName }
                .subtract(packageNames.toSet())
            appsToShow.forEach { app ->
                val index =
                    current.appsList.indexOfFirst { it.packageName == app }
                builder.setApps(
                    index,
                    current.getApps(index).toBuilder().setIsHidden(false)
                        .build()
                )
            }
            packageNames.forEach { app ->
                val index =
                    current.appsList.indexOfFirst { it.packageName == app }
                builder.setApps(
                    index,
                    current.getApps(index).toBuilder().setIsHidden(true)
                        .build()
                )
            }
            builder.build()
        }
    }

    suspend fun fetchInitial() = appsFlow.first()

    suspend fun reloadApps(): LauncherApps {
        return update { current ->
            val hidden = current.appsList
                .filter { it.isHidden }
                .map { it.packageName }
                .toSet()
            val apps = getApps().map { app ->
                if (hidden.contains(app.packageName)) {
                    app.toBuilder().setIsHidden(true).build()
                } else {
                    app
                }
            }
            current.clearApps().addAllApps(apps)
        }
    }

    private fun getApps(): List<LauncherApp> {
        val intent = Intent(Intent.ACTION_MAIN, null)
            .addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = when {
            Build.VERSION.SDK_INT >= 33 -> context.packageManager
                .queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )

            else -> context.packageManager.queryIntentActivities(
                intent, 0
            )
        }
        return apps.map { info ->
            val label = info.activityInfo.loadLabel(context.packageManager)
            LauncherApp.newBuilder()
                .setLabel(label.toString())
                .setPackageName(info.activityInfo.packageName)
                .addAllShortcuts(getShortcuts(info.activityInfo.packageName))
                .build()
        }.sortedBy { app -> app.label }
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