package com.shishifubing.atbl.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppCard(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    settings: LauncherAppCardSettings,
    actions: AppActions,
    modifier: Modifier = Modifier,
) {
    LongPressTextButton(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Text(
            modifier = Modifier.padding(settings.padding.dp),
            style = settings.textStyle.toTextStyle(),
            fontFamily = settings.fontFamily.toFontFamily(),
            color = settings.textColor.toColor(),
            text = actions.transformLabel(label, settings)
        )
    }
}

@Composable
private fun LongPressTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    // workaround to use long press on buttons: https://stackoverflow.com/a/76395585
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    LaunchedEffect(interactionSource) {
        var isLongClick = false
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    onLongClick()
                }

                is PressInteraction.Release -> {
                    if (isLongClick.not()) {
                        onClick()
                    }
                }
            }
        }
    }
    TextButton(
        modifier = modifier,
        onClick = { },
        interactionSource = interactionSource,
        content = content
    )
}

@Preview
@Composable
private fun AppCardPreview() {
    LauncherTheme(darkTheme = true) {
        AppCard(
            modifier = Modifier.padding(30.dp),
            label = "app",
            onClick = { /*TODO*/ },
            onLongClick = { /*TODO*/ },
            settings = LauncherAppCardSettings(
                removeSpaces = true,
                lowercase = true,
                padding = 0,
                textStyle = LauncherTextStyle.DisplayLarge,
                fontFamily = LauncherFontFamily.Monospace,
                textColor = LauncherTextColor.Unspecified,
                shortcutSeparator = "/"
            ),
            actions = object : AppActions {
                override fun launchAppUninstall(packageName: String) {}

                override fun setIsHidden(
                    packageName: String,
                    isHidden: Boolean
                ) {
                }

                override fun launchAppInfo(packageName: String) {}

                override fun launchApp(packageName: String) {}

                override fun launchShortcut(shortcut: LauncherAppShortcut) {}

                override fun showHiddenAppsToggle() {}

                override fun getAppIcon(packageName: String): ImageBitmap =
                    ImageBitmap(0, 0)

                override fun transformLabel(
                    label: String,
                    settings: LauncherAppCardSettings
                ): String = label
            }
        )
    }
}