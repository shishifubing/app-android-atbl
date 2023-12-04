package com.shishifubing.atbl

import android.app.Application
import android.appwidget.AppWidgetHost
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class LauncherApplication : Application() {
    lateinit var manager: LauncherManager
    lateinit var stateRepo: LauncherStateRepository
    lateinit var appWidgetHost: AppWidgetHost

    fun init(activity: ComponentActivity): LauncherApplication {
        manager = LauncherManager(activity, activity.lifecycle)
        stateRepo = LauncherStateRepository(manager, activity)
        appWidgetHost = AppWidgetHost(activity, 0)
        return this
    }
}

inline fun <reified T : ViewModel> launcherViewModelFactory(
    crossinline init: LauncherApplication.() -> T
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            (this[APPLICATION_KEY] as LauncherApplication).run(init)
        }
    }
}
