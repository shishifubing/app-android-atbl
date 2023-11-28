package com.shishifubing.atbl.ui

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.shishifubing.atbl.LauncherApplication

@Composable
fun AddWidgetScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val manager = AppWidgetManager.getInstance(LocalContext.current)
    val app = LocalContext.current.applicationContext as LauncherApplication
    LauncherScaffold(
        navController = navController
    ) {
        manager.installedProviders.forEach {
            Row {
                Text(it.loadLabel(LocalContext.current.packageManager))
                if (app.appWidgetHost != null) {
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