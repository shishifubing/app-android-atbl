package com.shishifubing.atbl

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings


private val tag = LauncherAppsManager::class.simpleName

class LauncherAppsManager(private val context: Context) {
    private val packageManager = context.packageManager
    private val launcherAppsService =
        context.getSystemService(LauncherApps::class.java)
    private val callbacks: MutableList<LauncherApps.Callback> = mutableListOf()

    fun launchApp(packageName: String) {
        context.startActivity(
            packageManager.getLaunchIntentForPackage(packageName)
        )
    }

    fun launchAppInfo(packageName: String) {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${packageName}")
            )
        )
    }

    fun launchAppUninstall(packageName: String) {
        context.startActivity(
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
        while (callbacks.isNotEmpty()) {
            launcherAppsService.unregisterCallback(callbacks.removeLast())
        }
    }

    fun addCallback(
        onRemoved: (String) -> Unit,
        onAdded: (String) -> Unit,
        onChanged: (String) -> Unit
    ) {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(
                packageName: String, user: UserHandle
            ) = onRemoved(packageName)

            override fun onPackageAdded(
                packageName: String, user: UserHandle
            ) = onAdded(packageName)

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
        }
        launcherAppsService.registerCallback(callback)
    }
}