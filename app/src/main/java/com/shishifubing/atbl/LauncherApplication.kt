package com.shishifubing.atbl

import android.app.Application
import android.appwidget.AppWidgetHost
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class LauncherApplication : Application() {
    var appsManager: LauncherManager? = null
    var stateRepo: LauncherStateRepository? = null
    var settingsRepo: LauncherSettingsRepository? = null
    var appWidgetHost: AppWidgetHost? = null
}

inline fun <reified T : ViewModel> launcherViewModelFactory(
    crossinline init: LauncherApplication.() -> T
) = viewModelFactory {
    initializer {
        (this[APPLICATION_KEY] as LauncherApplication).run(init)
    }
}
