package com.shishifubing.atbl

import android.app.Application
import android.appwidget.AppWidgetHost
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import com.shishifubing.atbl.domain.LauncherStateRepository

class LauncherApplication : Application() {
    var appsManager: LauncherAppsManager? = null
    var stateRepo: LauncherStateRepository? = null
    var settingsRepo: LauncherSettingsRepository? = null
    var appWidgetHost: AppWidgetHost? = null
}