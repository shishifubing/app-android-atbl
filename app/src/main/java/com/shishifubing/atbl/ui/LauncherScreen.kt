package com.shishifubing.atbl.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    vm: LauncherViewModel = viewModel(),
) {
    val settings by vm.settingsFlow.collectAsState(vm.initialSettings)
    val apps by vm.appsFlow.collectAsState(vm.initialApps)
    var dialogApp by remember { mutableStateOf<LauncherApp?>(null) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        FlowRow(
            horizontalArrangement = getHorizontalArrangement(
                settings.appLayoutHorizontalArrangement
            ),
            verticalArrangement = Arrangement.Center
        ) {
            apps.appsList.forEach { app ->
                AppCard(
                    app = app,
                    onClick = { vm.launchApp(app.packageName) },
                    onLongClick = { dialogApp = app },
                    settings = settings
                )
            }
        }
    }
    if (dialogApp != null) {
        val app = dialogApp!!
        AppDialog(
            app = app,
            onDismissRequest = { dialogApp = null },
            launchAppInfo = { vm.launchAppInfo(app.packageName) },
            launchAppUninstall = { vm.launchAppUninstall(app.packageName) },
            launchAppShortcut = vm::launchAppShortcut,
            hideApp = { vm.hideApp(app.packageName) },
            showHideAppButton = app.packageName != LocalContext.current.packageName
        )
    }
}

@Composable
fun AppCard(
    app: LauncherApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    settings: LauncherSettings,
    modifier: Modifier = Modifier,
) {
    if (app.isHidden) {
        return
    }
    val label = when (settings.appCardLabelRemoveSpaces) {
        true -> app.label.replace(" ", "")
        else -> app.label
    }
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
        interactionSource = interactionSource
    ) {
        Text(
            modifier = Modifier.padding(settings.appCardPadding.dp),
            text = if (settings.appCardLabelLowercase) label.lowercase() else label,
            style = getTextStyle(settings.appCardTextStyle),
            fontFamily = getFontFamily(settings.appCardFontFamily),
            color = getTextColor(settings.appCardTextColor)
        )
    }
}


@Composable
fun AppDialog(
    app: LauncherApp,
    onDismissRequest: () -> Unit,
    launchAppInfo: () -> Unit,
    launchAppUninstall: () -> Unit,
    launchAppShortcut: (LauncherAppShortcut) -> Unit,
    hideApp: () -> Unit,
    showHideAppButton: Boolean,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(modifier = modifier) {
            Column {
                AppDialogItem(
                    text = stringResource(R.string.drawer_app_info),
                    onClick = {
                        launchAppInfo()
                        onDismissRequest()
                    }
                )
                AppDialogItem(
                    text = stringResource(R.string.drawer_app_uninstall),
                    onClick = {
                        launchAppUninstall()
                        onDismissRequest()
                    }
                )
                if (showHideAppButton) {
                    AppDialogItem(
                        text = stringResource(R.string.drawer_app_hide),
                        onClick = {
                            hideApp()
                            onDismissRequest()
                        }
                    )
                }
                if (app.shortcutsList.isNotEmpty()) {
                    Divider()
                }
                LazyColumn(
                    modifier = Modifier.heightIn(
                        0.dp,
                        (LocalConfiguration.current.screenHeightDp * 0.6).dp
                    )
                ) {
                    items(app.shortcutsList.size) { i ->
                        val shortcut = app.shortcutsList[i]
                        AppDialogItem(text = shortcut.label) {
                            launchAppShortcut(shortcut)
                            onDismissRequest()
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun AppDialogItem(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun getTextStyle(textStyle: LauncherTextStyle): TextStyle {
    val typography = MaterialTheme.typography
    return when (textStyle) {
        LauncherTextStyle.DisplayLarge -> typography.displayLarge
        LauncherTextStyle.DisplayMedium -> typography.displayMedium
        LauncherTextStyle.DisplaySmall -> typography.displaySmall
        LauncherTextStyle.HeadlineLarge -> typography.headlineLarge
        LauncherTextStyle.HeadlineMedium -> typography.headlineMedium
        LauncherTextStyle.HeadlineSmall -> typography.headlineSmall
        LauncherTextStyle.TitleLarge -> typography.titleLarge
        LauncherTextStyle.TitleMedium -> typography.titleMedium
        LauncherTextStyle.TitleSmall -> typography.titleSmall
        LauncherTextStyle.BodyLarge -> typography.bodyLarge
        LauncherTextStyle.BodyMedium -> typography.bodyMedium
        LauncherTextStyle.BodySmall -> typography.bodySmall
        LauncherTextStyle.LabelLarge -> typography.labelLarge
        LauncherTextStyle.LabelMedium -> typography.labelMedium
        LauncherTextStyle.LabelSmall -> typography.labelSmall
        else -> typography.bodyMedium
    }
}

@Composable
fun getTextColor(textColor: LauncherTextColor): Color {
    return when (textColor) {
        LauncherTextColor.Unspecified -> Color.Unspecified
        LauncherTextColor.Black -> Color.Black
        LauncherTextColor.DarkGray -> Color.DarkGray
        LauncherTextColor.Gray -> Color.Gray
        LauncherTextColor.LightGray -> Color.LightGray
        LauncherTextColor.White -> Color.White
        LauncherTextColor.Red -> Color.Red
        LauncherTextColor.Green -> Color.Green
        LauncherTextColor.Blue -> Color.Blue
        LauncherTextColor.Yellow -> Color.Yellow
        LauncherTextColor.Cyan -> Color.Cyan
        LauncherTextColor.Magenta -> Color.Magenta
        LauncherTextColor.Transparent -> Color.Transparent
        else -> getTextColor(LauncherTextColor.Unspecified)
    }
}

@Composable
fun getFontFamily(fontFamily: LauncherFontFamily): FontFamily {
    return when (fontFamily) {
        LauncherFontFamily.Default -> FontFamily.Default
        LauncherFontFamily.Cursive -> FontFamily.Cursive
        LauncherFontFamily.Monospace -> FontFamily.Monospace
        LauncherFontFamily.SansSerif -> FontFamily.SansSerif
        LauncherFontFamily.Serif -> FontFamily.Serif
        else -> FontFamily.Default
    }
}

@Composable
fun getHorizontalArrangement(
    horizontalArrangement: LauncherHorizontalArrangement
): Arrangement.Horizontal {
    return when (horizontalArrangement) {
        LauncherHorizontalArrangement.Start -> Arrangement.Start
        LauncherHorizontalArrangement.End -> Arrangement.End
        LauncherHorizontalArrangement.Center -> Arrangement.Center
        LauncherHorizontalArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
        LauncherHorizontalArrangement.SpaceBetween -> Arrangement.SpaceBetween
        LauncherHorizontalArrangement.SpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }
}