package com.shishifubing.atbl

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings


private val tag = LauncherAppsManager::class.simpleName

class LauncherAppsManager(private val context: Context) {
    private val packageManager = context.packageManager
    private val launcherAppsService =
        context.getSystemService(LauncherApps::class.java)

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
        context.getSystemService(LauncherApps::class.java).startShortcut(
            shortcut.packageName, shortcut.shortcutId, null, null,
            android.os.Process.myUserHandle()
        )
    }

    fun addCallback(action: () -> Unit) {
        launcherAppsService.registerCallback(getLauncherAppsCallback(action))
    }

    private fun getLauncherAppsCallback(action: () -> Unit): LauncherApps.Callback {
        return object : LauncherApps.Callback() {
            override fun onPackageRemoved(p1: String?, p2: UserHandle?) =
                action()

            override fun onPackageAdded(p1: String?, p2: UserHandle?) =
                action()

            override fun onPackageChanged(p1: String?, p2: UserHandle?) =
                action()

            override fun onPackagesAvailable(
                p1: Array<out String>?, p2: UserHandle?, p3: Boolean
            ) = Unit

            override fun onPackagesUnavailable(
                p1: Array<out String>?, p2: UserHandle?, p3: Boolean
            ) = Unit
        }
    }
}