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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UiState

object AddWidgetRoute : LauncherRoute<Model.State, StateViewModel> {
    override val url = "add_widget_screen"
    override val label = R.string.navigation_add_widget
    override val showScaffold = true

    @Composable
    override fun getViewModel(): StateViewModel {
        return viewModel(factory = StateViewModel.Factory)
    }

    @Composable
    override fun Content(
        vm: StateViewModel,
        uiState: UiState.Success<Model.State>
    ) {
        AddWidgetScreen()
    }
}

@Composable
private fun AddWidgetScreen(
    modifier: Modifier = Modifier
) {
    val manager = AppWidgetManager.getInstance(LocalContext.current)
    val app = LocalContext.current.applicationContext as LauncherApplication
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