package com.shishifubing.atbl.domain

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.content.Intent.CATEGORY_HOME
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.net.Uri
import android.os.Build
import android.os.UserHandle
import android.provider.Settings
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherSplitScreenShortcut


private val tag = LauncherAppsManager::class.simpleName

class LauncherAppsManager(
    private val context: Context,
    private val lifecycle: Lifecycle
) {
    private val packageManager = context.packageManager
    private val launcherAppsService =
        context.getSystemService(LauncherApps::class.java)
    private val callbacks: MutableList<LauncherApps.Callback> = mutableListOf()

    fun isHomeApp(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 29 -> context
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
                        context.packageName
                    )
            }.isNotEmpty()
        }
    }

    fun launchApp(packageName: String, flags: Int? = null) {
        context.startActivity(
            packageManager
                .getLaunchIntentForPackage(packageName)
                .let { if (flags == null) it else it?.setFlags(flags) }
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
        for (callback in callbacks) {
            launcherAppsService.unregisterCallback(callback)
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

    fun launchSplitScreen(shortcut: LauncherSplitScreenShortcut) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                lifecycle.removeObserver(this)
                launchApp(
                    shortcut.appTop.packageName,
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent
                        .FLAG_ACTIVITY_LAUNCH_ADJACENT
                )
            }
        })
        context.startActivity(Intent(ACTION_MAIN).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        launchApp(shortcut.appBottom.packageName)
    }
}

