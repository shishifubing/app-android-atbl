package com.shishifubing.atbl.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
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