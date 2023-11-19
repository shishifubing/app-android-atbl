package com.shishifubing.atbl

import android.app.Application
import android.appwidget.AppWidgetHost
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository

class LauncherApplication : Application() {
    var appsManager: LauncherAppsManager? = null
    var appsRepo: LauncherAppsRepository? = null
    var settingsRepo: LauncherSettingsRepository? = null
    var appWidgetHost: AppWidgetHost? = null
}