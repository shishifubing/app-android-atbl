package com.shishifubing.atbl

import android.app.Application
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository

class LauncherApplication : Application() {
    var launcherAppsManager: LauncherAppsManager? = null
    var launcherAppsRepo: LauncherAppsRepository? = null
    var launcherSettingsRepo: LauncherSettingsRepository? = null
}