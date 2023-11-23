package com.shishifubing.atbl.domain

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.content.Intent.CATEGORY_HOME
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherSplitScreenShortcut


private val tag = LauncherAppsManager::class.simpleName

class LauncherAppsManager(
    private val parent: ComponentActivity
) {
    private val packageManager = parent.packageManager
    private val launcherAppsService = parent.getSystemService(
        LauncherApps::class.java
    )
    private val callbacks: MutableList<LauncherApps.Callback> = mutableListOf()


    fun isHomeApp(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 29 -> parent
                .getSystemService(RoleManager::class.java)
                .isRoleHeld(RoleManager.ROLE_HOME)

            else -> listOf<ComponentName>().apply {
                packageManager
                    .getPreferredActivities(
                        listOf(
                            IntentFilter(ACTION_MAIN).apply {
                                addCategory(CATEGORY_HOME)
                            }
                        ),
                        this,
                        parent.packageName
                    )
            }.isNotEmpty()
        }
    }

    fun launchApp(packageName: String, flags: Int? = null) {
        parent.startActivity(
            packageManager
                .getLaunchIntentForPackage(packageName)
                .let { if (flags == null) it else it?.setFlags(flags) }
        )
    }

    fun launchAppInfo(packageName: String) {
        parent.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${packageName}")
            )
        )
    }

    fun launchAppUninstall(packageName: String) {
        parent.startActivity(
            Intent(Intent.ACTION_DELETE, Uri.parse("package:${packageName}"))
        )
    }

    fun launchAppShortcut(shortcut: LauncherAppShortcut) {
        launcherAppsService.startShortcut(
            shortcut.packageName, shortcut.shortcutId, null, null,
            android.os.Process.myUserHandle()
        )
    }

    fun removeCallbacks() {
        for (callback in callbacks) {
            launcherAppsService.unregisterCallback(callback)
        }
    }

    fun queryPackageManager(intent: Intent): List<ResolveInfo> {
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

    fun getAppShortcuts(packageName: String): List<LauncherAppShortcut> {
        val userHandle = android.os.Process.myUserHandle()
        val query = LauncherApps.ShortcutQuery()
            .setPackage(packageName)
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
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

    fun getApp(packageName: String): LauncherApp {
        val queryResults = queryPackageManager(
            Intent(ACTION_MAIN, null)
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
            .addAllShortcuts(getAppShortcuts(info.packageName))
            .build()
    }

    fun fetchAllApps(): Map<String, LauncherApp> {
        val queryResults = queryPackageManager(
            Intent(ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)
        )
        return queryResults.associate { info ->
            val activity = info.activityInfo
            val name = activity.packageName
            name to LauncherApp.newBuilder()
                .setLabel(activity.loadLabel(parent.packageManager).toString())
                .setPackageName(name)
                .addAllShortcuts(getAppShortcuts(name))
                .build()
        }
    }

    fun getAppIcon(packageName: String): ImageBitmap {
        return packageManager.getApplicationIcon(packageName)
            .toBitmap(config = Bitmap.Config.ARGB_8888)
            .asImageBitmap()
    }

    fun addCallback(
        onRemoved: (String) -> Unit,
        onChanged: (String) -> Unit
    ) = launcherAppsService.registerCallback(object : LauncherApps.Callback() {
        override fun onPackageRemoved(
            packageName: String, user: UserHandle
        ) = onRemoved(packageName)

        override fun onPackageAdded(
            packageName: String, user: UserHandle
        ) = onChanged(packageName)

        override fun onPackageChanged(
            packageName: String, p2: UserHandle
        ) = onChanged(packageName)

        override fun onShortcutsChanged(
            packageName: String,
            shortcuts: MutableList<ShortcutInfo>,
            user: UserHandle
        ) = onChanged(packageName)

        override fun onPackagesAvailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean
        ) = Unit

        override fun onPackagesUnavailable(
            packageNames: Array<out String>?,
            user: UserHandle?,
            replacing: Boolean
        ) = Unit
    })

    fun launchSplitScreen(shortcut: LauncherSplitScreenShortcut) {
        parent.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                parent.lifecycle.removeObserver(this)
                launchApp(
                    shortcut.appTop.packageName,
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT
                )
            }
        })
        launchApp(
            shortcut.appBottom.packageName,
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
    }
}

