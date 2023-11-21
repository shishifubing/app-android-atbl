package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LauncherRoute(
    navigate: (route: LauncherNav) -> Unit,
    modifier: Modifier = Modifier,
    vm: LauncherViewModel = viewModel(factory = LauncherViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    ErrorToast(errorFlow = vm.error)
    LauncherScreen(
        modifier = modifier,
        navigate = navigate,
        uiState = uiState,
        appActions = vm.appActions,
        splitScreenShortcutActions = vm.splitScreenShortcutActions
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LauncherScreen(
    navigate: (route: LauncherNav) -> Unit,
    modifier: Modifier = Modifier,
    uiState: LauncherScreenUiState,
    appActions: AppActions,
    splitScreenShortcutActions: SplitScreenShortcutActions
) {
    if (uiState !is LauncherScreenUiState.Success) {
        return
    }
    LauncherRow(
        modifier = modifier,
        rowSettings = uiState.launcherRowSettings,
        showHiddenAppsToggle = appActions::showHiddenAppsToggle,
        showHiddenApps = uiState.showHiddenApps,
        navigate = navigate
    ) {
        if (!uiState.isHomeApp) {
            NotAHomeAppBanner()
        }
        SplitScreenShortcuts(
            shortcuts = uiState.splitScreenShortcuts,
            appCardSettings = uiState.appCardSettings,
            appActions = appActions,
            shortcutActions = splitScreenShortcutActions
        )
        LauncherApps(
            apps = uiState.apps,
            actions = appActions,
            showShortcuts = uiState.isHomeApp,
            launchShortcut = appActions::launchShortcut,
            appCardSettings = uiState.appCardSettings
        )
    }
}


@Composable
private fun LauncherApps(
    apps: Collection<LauncherApp>,
    actions: AppActions,
    showShortcuts: Boolean,
    launchShortcut: (LauncherAppShortcut) -> Unit,
    appCardSettings: LauncherAppCardSettings
) {
    var dialogApp by remember { mutableStateOf<LauncherApp?>(null) }
    apps.forEach { app ->
        AppCard(
            label = app.label,
            onClick = { actions.launchApp(app.packageName) },
            onLongClick = { dialogApp = app },
            settings = appCardSettings,
            actions = actions
        )
    }
    if (dialogApp != null) {
        AppDialog(
            app = dialogApp!!,
            actions = actions,
            launchAppShortcut = launchShortcut,
            onDismissRequest = { dialogApp = null },
            showShortcuts = showShortcuts
        )
    }
}

@Composable
private fun SplitScreenShortcuts(
    shortcuts: Collection<LauncherSplitScreenShortcut>,
    appCardSettings: LauncherAppCardSettings,
    appActions: AppActions,
    shortcutActions: SplitScreenShortcutActions,
) {
    var dialogShortcut by remember {
        mutableStateOf<LauncherSplitScreenShortcut?>(null)
    }
    shortcuts.forEach { shortcut ->
        AppCard(
            label = listOf(
                shortcut.appTop.label,
                shortcut.appBottom.label
            ).joinToString(appCardSettings.shortcutSeparator),
            onClick = { shortcutActions.launchSplitScreenShortcut(shortcut) },
            onLongClick = { dialogShortcut = shortcut },
            settings = appCardSettings,
            actions = appActions
        )
    }
    if (dialogShortcut != null) {
        SplitScreenShortcutDialog(
            shortcut = dialogShortcut!!,
            actions = appActions,
            deleteShortcut = shortcutActions::removeSplitScreenShortcut,
            onDismissRequest = { dialogShortcut = null },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun LauncherRow(
    rowSettings: LauncherRowSettings,
    showHiddenApps: Boolean,
    showHiddenAppsToggle: () -> Unit,
    navigate: (route: LauncherNav) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    var showLauncherDialog by remember { mutableStateOf(false) }
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                rowSettings.horizontalPadding.dp,
                rowSettings.verticalPadding.dp
            )
            .combinedClickable(
                onLongClick = { showLauncherDialog = true },
                onClick = {}
            ),
        horizontalArrangement = getHorizontalArrangement(rowSettings.horizontalArrangement),
        verticalArrangement = getVerticalArrangement(rowSettings.verticalArrangement),
        content = content
    )
    if (showLauncherDialog) {
        LauncherActionsDialog(
            navigate = navigate,
            showHiddenApps = showHiddenApps,
            showHiddenAppsToggle = showHiddenAppsToggle,
            onDismissRequest = { showLauncherDialog = false }
        )
    }
}

@Composable
private fun LauncherActionsDialog(
    navigate: (route: LauncherNav) -> Unit,
    showHiddenApps: Boolean,
    showHiddenAppsToggle: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        R.string.launcher_dialog_settings to { navigate(LauncherNav.Settings) },
        R.string.launcher_dialog_add_widget to { navigate(LauncherNav.AddWidget) },
        if (showHiddenApps) {
            R.string.launcher_dialog_hide_hidden_apps
        } else {
            R.string.launcher_dialog_show_hidden_apps
        } to showHiddenAppsToggle
    )
    LauncherDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        AppDialogItems(
            modifier = modifier,
            itemsCount = items.size,
            itemKey = { i -> items[i].hashCode() }
        ) { i ->
            val item = items[i]
            AppDialogButton(
                text = stringResource(item.first),
                textAlign = TextAlign.Start,
                onClick = { item.second(); onDismissRequest() }
            )
        }
    }
}

@Composable
private fun NotAHomeAppBanner() {
    Card {
        Row(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.padding_medium))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Warning icon",
                modifier = Modifier.size(ButtonDefaults.IconSize * 2),
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
            Text(stringResource(R.string.not_a_home_app_banner))
        }
    }
}

@Composable
private fun AppCard(
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
            style = getTextStyle(settings.textStyle),
            fontFamily = getFontFamily(settings.fontFamily),
            color = getTextColor(settings.textColor),
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

@Composable
private fun AppDialog(
    app: LauncherApp,
    actions: AppActions,
    onDismissRequest: () -> Unit,
    launchAppShortcut: (LauncherAppShortcut) -> Unit,
    showShortcuts: Boolean,
    modifier: Modifier = Modifier,
) {
    LauncherDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        AppDialogHeader(
            app = app,
            actions = actions,
            onDismissRequest = onDismissRequest
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        if (app.shortcutsList.isNotEmpty() && showShortcuts) {
            AppDialogShortcuts(
                app = app,
                onDismissRequest = onDismissRequest,
                launchAppShortcut = launchAppShortcut
            )
        }
    }
}

@Composable
private fun LauncherDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
private fun SplitScreenShortcutDialog(
    shortcut: LauncherSplitScreenShortcut,
    deleteShortcut: (LauncherSplitScreenShortcut) -> Unit,
    actions: AppActions,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LauncherDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        AppDialogHeader(
            app = shortcut.appTop,
            actions = actions,
            onDismissRequest = onDismissRequest
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        AppDialogHeader(
            app = shortcut.appBottom,
            actions = actions,
            onDismissRequest = onDismissRequest
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        AppDialogItems(itemsCount = 1, itemKey = { shortcut.hashCode() }) {
            AppDialogButton(
                text = stringResource(R.string.drawer_app_delete_split_screen_shortcut),
                onClick = {
                    deleteShortcut(shortcut)
                    onDismissRequest()
                },
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun AppDialogHeader(
    app: LauncherApp,
    actions: AppActions,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElevatedCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, dimensionResource(R.dimen.padding_medium)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .size(dimensionResource(R.dimen.image_size)),
                    bitmap = actions.getAppIcon(app.packageName),
                    contentDescription = "App icon",
                )
                Text(
                    text = app.label,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        ElevatedCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppDialogButton(
                    text = stringResource(R.string.drawer_app_info),
                    onClick = {
                        actions.launchAppInfo(app.packageName)
                        onDismissRequest()
                    }
                )
                AppDialogButton(
                    text = stringResource(
                        if (app.isHidden) R.string.drawer_app_show else R.string.drawer_app_hide
                    ),
                    onClick = {
                        actions.toggleIsHidden(app.packageName)
                        onDismissRequest()
                    }
                )
                AppDialogButton(
                    text = stringResource(R.string.drawer_app_uninstall),
                    onClick = {
                        actions.launchAppUninstall(app.packageName)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun AppDialogShortcuts(
    app: LauncherApp,
    onDismissRequest: () -> Unit,
    launchAppShortcut: (LauncherAppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDialogItems(
        modifier = modifier,
        itemsCount = app.shortcutsList.size,
        itemKey = { i -> app.shortcutsList[i].hashCode() }) { i ->
        val shortcut = app.shortcutsList[i]
        AppDialogButton(
            text = shortcut.label,
            textAlign = TextAlign.Start,
            onClick = {
                launchAppShortcut(shortcut)
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun AppDialogItems(
    itemsCount: Int,
    itemKey: (Int) -> Any,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(Int) -> Unit,
) {
    ElevatedCard(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    0.dp,
                    (LocalConfiguration.current.screenHeightDp * 0.6).dp
                )
        ) {
            items(count = itemsCount, key = itemKey, itemContent = itemContent)
        }
    }
}

@Composable
private fun AppDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = textAlign
        )
    }

}

@Composable
private fun getTextStyle(textStyle: LauncherTextStyle): TextStyle {
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

private fun getTextColor(textColor: LauncherTextColor): Color {
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

private fun getFontFamily(fontFamily: LauncherFontFamily): FontFamily {
    return when (fontFamily) {
        LauncherFontFamily.Default -> FontFamily.Default
        LauncherFontFamily.Cursive -> FontFamily.Cursive
        LauncherFontFamily.Monospace -> FontFamily.Monospace
        LauncherFontFamily.SansSerif -> FontFamily.SansSerif
        LauncherFontFamily.Serif -> FontFamily.Serif
        else -> FontFamily.Default
    }
}

private fun getHorizontalArrangement(
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

private fun getVerticalArrangement(
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