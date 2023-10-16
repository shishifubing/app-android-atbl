package com.shishifubing.atbl.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val colorSchemes = object {
    val dark = darkColorScheme()
    val light = lightColorScheme()
}

@Composable
fun LauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(LocalContext.current)
            } else {
                dynamicLightColorScheme(LocalContext.current)
            }
        }

        darkTheme -> colorSchemes.dark
        else -> colorSchemes.light
    }
    // transparent status bar: https://stackoverflow.com/a/72776074
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        val insetController = WindowCompat.getInsetsController(
            window, view
        )
        window.navigationBarColor = colorScheme.primary.copy(alpha = 0.08f)
            .compositeOver(colorScheme.surface.copy()).toArgb()
        window.statusBarColor = colorScheme.background.toArgb()
        insetController.isAppearanceLightStatusBars = !darkTheme
        insetController.isAppearanceLightNavigationBars = !darkTheme
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}