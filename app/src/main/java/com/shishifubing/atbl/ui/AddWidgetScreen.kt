package com.shishifubing.atbl.ui

import android.appwidget.AppWidgetManager
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AddWidgetScreen(
    modifier: Modifier = Modifier
) {
    val manager = AppWidgetManager.getInstance(LocalContext.current)
    manager.installedProviders.forEach {
        Row {

            Icon(
                it.loadPreviewImage(
                    LocalContext.current,
                    LocalContext.current.resources.displayMetrics.densityDpi
                ).toBitmap(config = Bitmap.Config.ARGB_8888)
                    .asImageBitmap(),
                contentDescription = "icon"
            )
            Text(it.loadLabel(LocalContext.current.packageManager))
        }
    }
}