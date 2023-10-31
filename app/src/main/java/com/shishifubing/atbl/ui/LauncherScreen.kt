package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    modifier: Modifier = Modifier,
    vm: LauncherViewModel = viewModel(),
) {
    val settings by vm.settingsFlow.collectAsState(vm.initialSettings)
    val apps by vm.appsFlow.collectAsState(vm.initialApps)
    var dialogApp by remember { mutableStateOf<LauncherApp?>(null) }
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                settings.appLayoutHorizontalPadding.dp,
                settings.appLayoutVerticalPadding.dp
            ),
        horizontalArrangement = getHorizontalArrangement(
            settings.appLayoutHorizontalArrangement
        ),
        verticalArrangement = getVerticalArrangement(
            settings.appLayoutVerticalArrangement
        )
    ) {
        Button(onClick = {
            vm.launchSplitScreen(
                "com.ichi2.anki",
                "org.schabi.newpipe",
            )
        }) {
            Text("test")
        }
        apps.appsList.forEach { app ->
            AppCard(
                app = app,
                onClick = { vm.launchApp(app.packageName) },
                onLongClick = { dialogApp = app },
                settings = settings
            )
        }
    }

    if (dialogApp != null) {
        val app = dialogApp!!
        AppDialog(
            app = app,
            vm = vm,
            onDismissRequest = { dialogApp = null },
            enabledHide = app.packageName != LocalContext.current.packageName
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
    val label = app.label
        .let {
            if (settings.appCardLabelRemoveSpaces) {
                it.replace(" ", "")
            } else {
                it
            }
        }
        .let {
            if (settings.appCardLabelLowercase) {
                it.lowercase()
            } else {
                it
            }
        }
    // workaround to use long press on buttons: https://stackoverflow.com/a/76395585
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    LaunchedEffect(interactionSource, app) {
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
    ) {
        Text(
            modifier = Modifier.padding(settings.appCardPadding.dp),
            text = label,
            style = getTextStyle(settings.appCardTextStyle),
            fontFamily = getFontFamily(settings.appCardFontFamily),
            color = getTextColor(settings.appCardTextColor)
        )
    }
}


@Composable
fun AppDialog(
    app: LauncherApp,
    vm: LauncherViewModel,
    onDismissRequest: () -> Unit,
    enabledHide: Boolean,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(modifier = modifier) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppDialogHeader(
                    app = app,
                    vm = vm,
                    onDismissRequest = onDismissRequest,
                    enabledHide = enabledHide
                )
                if (app.shortcutsList.isNotEmpty()) {
                    Divider()
                }
                AppDialogShortcuts(
                    vm = vm,
                    app = app,
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
fun AppDialogHeader(
    app: LauncherApp,
    vm: LauncherViewModel,
    onDismissRequest: () -> Unit,
    enabledHide: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(dimensionResource(R.dimen.padding_small)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.padding_small))
                    .size(dimensionResource(R.dimen.image_size)),
                bitmap = vm.getAppIcon(app.packageName),
                contentDescription = "App icon",
            )
            Text(
                text = app.label,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(dimensionResource(R.dimen.padding_small)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AppDialogButton(
                text = stringResource(R.string.drawer_app_info),
                shapeType = 0,
                onClick = {
                    vm.launchAppInfo(app.packageName)
                    onDismissRequest()
                }
            )
            Spacer(modifier = Modifier.height(1.dp))
            AppDialogButton(
                text = stringResource(R.string.drawer_app_hide),
                enabled = enabledHide,
                shapeType = 1,
                onClick = {
                    vm.hideApp(app.packageName)
                    onDismissRequest()
                }
            )
            Spacer(modifier = Modifier.height(1.dp))
            AppDialogButton(
                text = stringResource(R.string.drawer_app_uninstall),
                shapeType = 2,
                onClick = {
                    vm.launchAppUninstall(app.packageName)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
fun AppDialogShortcuts(
    vm: LauncherViewModel,
    app: LauncherApp,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.heightIn(
            0.dp,
            (LocalConfiguration.current.screenHeightDp * 0.6).dp
        )
    ) {
        items(app.shortcutsList.size) { i ->
            val shortcut = app.shortcutsList[i]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    vm.launchAppShortcut(shortcut)
                    onDismissRequest()
                },
            ) {
                Text(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                    text = shortcut.label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

    }
}

@Composable
fun AppDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shapeType: Int = 1,
    enabled: Boolean = true
) {
    val shape = when (shapeType) {
        0 -> RoundedCornerShape(
            dimensionResource(R.dimen.padding_medium),
            dimensionResource(R.dimen.padding_medium),
            0.dp,
            0.dp,
        )

        2 -> RoundedCornerShape(
            0.dp,
            0.dp,
            dimensionResource(R.dimen.padding_medium),
            dimensionResource(R.dimen.padding_medium),
        )

        else -> RectangleShape
    }

    ElevatedButton(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        shape = shape
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
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
        LauncherHorizontalArrangement.HorizontalStart -> Arrangement.Start
        LauncherHorizontalArrangement.HorizontalEnd -> Arrangement.End
        LauncherHorizontalArrangement.HorizontalCenter -> Arrangement.Center
        LauncherHorizontalArrangement.HorizontalSpaceEvenly -> Arrangement.SpaceEvenly
        LauncherHorizontalArrangement.HorizontalSpaceBetween -> Arrangement.SpaceBetween
        LauncherHorizontalArrangement.HorizontalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }
}

@Composable
fun getVerticalArrangement(
    verticalArrangement: LauncherVerticalArrangement
): Arrangement.Vertical {
    return when (verticalArrangement) {
        LauncherVerticalArrangement.VerticalTop -> Arrangement.Top
        LauncherVerticalArrangement.VerticalBottom -> Arrangement.Bottom
        LauncherVerticalArrangement.VerticalCenter -> Arrangement.Center
        LauncherVerticalArrangement.VerticalSpaceEvenly -> Arrangement.SpaceEvenly
        LauncherVerticalArrangement.VerticalSpaceBetween -> Arrangement.SpaceBetween
        LauncherVerticalArrangement.VerticalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }
}