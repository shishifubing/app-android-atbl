package com.shishifubing.atbl.ui

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.shishifubing.atbl.LauncherApplication

@Composable
fun AddWidgetScreen(
    nav: LauncherNav,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val manager = AppWidgetManager.getInstance(LocalContext.current)
    val app = LocalContext.current.applicationContext as LauncherApplication
    LauncherScaffold(nav = nav, goBack = { navController.popBackStack() }) {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            manager.installedProviders.forEach {
                Row {
                    Text(it.loadLabel(LocalContext.current.packageManager))
                    AndroidView(factory = { context ->
                        app.appWidgetHost.createView(
                            context,
                            app.appWidgetHost.allocateAppWidgetId(),
                            it
                        )
                    })
                }
            }
        }
    }
}