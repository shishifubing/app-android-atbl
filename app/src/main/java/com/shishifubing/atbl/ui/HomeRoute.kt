package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.R

@Composable
fun HomeRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    val showHiddenApps by vm.showHiddenApps.collectAsState()

    ErrorToast(errorFlow = vm.error)

    when (uiState) {
        LauncherUiState.Loading -> {
            PageLoadingIndicator(
                modifier = modifier
            )
        }

        is LauncherUiState.Success -> {
            HomeScreen(
                modifier = modifier,
                uiState = uiState as LauncherUiState.Success,
                showHiddenApps = showHiddenApps,
                navigate = navController::navigate,
                launcherActions = vm.launcherActions,
                splitScreenShortcutActions = vm.splitScreenShortcutActions,
                appActions = vm.appActions
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    uiState: LauncherUiState.Success,
    navigate: (LauncherNav) -> Unit,
    appActions: AppActions,
    launcherActions: LauncherActions,
    splitScreenShortcutActions: SplitScreenShortcutActions,
    showHiddenApps: Boolean,
    modifier: Modifier = Modifier
) {
    var showLauncherDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { uiState.screens.size })

    Box(modifier = modifier) {
        HorizontalPager(state = pagerState) {
            HomePage(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onLongClick = { showLauncherDialog = true },
                        onClick = { }
                    ),
                screenState = uiState.screens[it],
                appActions = appActions,
                splitScreenShortcutActions = splitScreenShortcutActions
            )
        }
        HomePageIndicatorFloating(
            currentPage = pagerState.currentPage,
            pageCount = pagerState.pageCount
        )
    }
    
    if (showLauncherDialog) {
        HomeLauncherDialogActions(
            navigate = navigate,
            showHiddenApps = showHiddenApps,
            actions = launcherActions,
            currentPage = pagerState.currentPage,
            onDismissRequest = { showLauncherDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomePage(
    modifier: Modifier = Modifier,
    screenState: LauncherScreenUiState,
    appActions: AppActions,
    splitScreenShortcutActions: SplitScreenShortcutActions
) {
    HomeRow(
        modifier = modifier,
        rowSettings = screenState.launcherRowSettings,
    ) {
        if (!screenState.isHomeApp) {
            NotAHomeAppBanner()
        }
        screenState.items.forEach {
            HomeItem(
                item = it,
                appActions = appActions,
                splitScreenShortcutActions = splitScreenShortcutActions,
                isHomeApp = screenState.isHomeApp,
                appCardSettings = screenState.appCardSettings
            )
        }
    }
}

@Composable
fun HomeItem(
    item: LauncherUiItem,
    appActions: AppActions,
    splitScreenShortcutActions: SplitScreenShortcutActions,
    isHomeApp: Boolean,
    appCardSettings: LauncherAppCardSettings
) {
    when (item) {
        is LauncherUiItem.Apps ->
            HomeItemApps(
                apps = item.apps,
                actions = appActions,
                showShortcuts = isHomeApp,
                launchShortcut = appActions::launchShortcut,
                appCardSettings = appCardSettings
            )

        is LauncherUiItem.Shortcuts ->
            HomeItemSplitScreenShortcuts(
                shortcuts = item.shortcuts,
                appCardSettings = appCardSettings,
                appActions = appActions,
                shortcutActions = splitScreenShortcutActions
            )
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


@Preview(
    name = "phone",
    device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480"
)
@Preview(
    name = "landscape",
    device = "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480"
)
@Preview(
    name = "foldable",
    device = "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480"
)
@Preview(
    name = "tablet",
    device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480"
)
@Composable
private fun HomePagePreview() {
    val apps = (0..40).map {
        LauncherApp.getDefaultInstance()
            .toBuilder()
            .setLabel("app-$it")
            .build()
    }
    LauncherTheme(darkTheme = true) {
        HomePage(
            modifier = Modifier.fillMaxSize(),
            screenState = LauncherScreenUiState(
                items = listOf(
                    LauncherUiItem.Apps(apps = apps),
                    LauncherUiItem.Shortcuts(
                        shortcuts = listOf(
                            LauncherSplitScreenShortcut.getDefaultInstance()
                                .toBuilder()
                                .setAppTop(apps[0])
                                .setAppBottom(apps[1])
                                .build()
                        )
                    )
                ),
                launcherRowSettings = LauncherStateRepository.defaultSettings.rowSettings(),
                appCardSettings = LauncherStateRepository.defaultSettings.appCardSettings(),
                isHomeApp = true,
                showHiddenApps = false,
            ),
            appActions = appActionStub,
            splitScreenShortcutActions = splitScreenShortcutActionsStub
        )
    }
}

@Composable
fun LauncherTextStyle.toTextStyle(): TextStyle = MaterialTheme.typography.let {
    when (this) {
        LauncherTextStyle.DisplayLarge -> it.displayLarge
        LauncherTextStyle.DisplayMedium -> it.displayMedium
        LauncherTextStyle.DisplaySmall -> it.displaySmall
        LauncherTextStyle.HeadlineLarge -> it.headlineLarge
        LauncherTextStyle.HeadlineMedium -> it.headlineMedium
        LauncherTextStyle.HeadlineSmall -> it.headlineSmall
        LauncherTextStyle.TitleLarge -> it.titleLarge
        LauncherTextStyle.TitleMedium -> it.titleMedium
        LauncherTextStyle.TitleSmall -> it.titleSmall
        LauncherTextStyle.BodyLarge -> it.bodyLarge
        LauncherTextStyle.BodyMedium -> it.bodyMedium
        LauncherTextStyle.BodySmall -> it.bodySmall
        LauncherTextStyle.LabelLarge -> it.labelLarge
        LauncherTextStyle.LabelMedium -> it.labelMedium
        LauncherTextStyle.LabelSmall -> it.labelSmall
        else -> it.bodyMedium
    }
}

@Composable
fun LauncherTextColor.toColor(): Color = when (this) {
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
    else -> Color.Unspecified
}


@Composable
fun LauncherFontFamily.toFontFamily(): FontFamily = when (this) {
    LauncherFontFamily.Default -> FontFamily.Default
    LauncherFontFamily.Cursive -> FontFamily.Cursive
    LauncherFontFamily.Monospace -> FontFamily.Monospace
    LauncherFontFamily.SansSerif -> FontFamily.SansSerif
    LauncherFontFamily.Serif -> FontFamily.Serif
    else -> FontFamily.Default
}

@Composable
fun LauncherHorizontalArrangement.toArrangement(): Arrangement.Horizontal =
    when (this) {
        LauncherHorizontalArrangement.HorizontalStart -> Arrangement.Start
        LauncherHorizontalArrangement.HorizontalEnd -> Arrangement.End
        LauncherHorizontalArrangement.HorizontalCenter -> Arrangement.Center
        LauncherHorizontalArrangement.HorizontalSpaceEvenly -> Arrangement.SpaceEvenly
        LauncherHorizontalArrangement.HorizontalSpaceBetween -> Arrangement.SpaceBetween
        LauncherHorizontalArrangement.HorizontalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }

@Composable
fun LauncherVerticalArrangement.toArrangement(): Arrangement.Vertical =
    when (this) {
        LauncherVerticalArrangement.VerticalTop -> Arrangement.Top
        LauncherVerticalArrangement.VerticalBottom -> Arrangement.Bottom
        LauncherVerticalArrangement.VerticalCenter -> Arrangement.Center
        LauncherVerticalArrangement.VerticalSpaceEvenly -> Arrangement.SpaceEvenly
        LauncherVerticalArrangement.VerticalSpaceBetween -> Arrangement.SpaceBetween
        LauncherVerticalArrangement.VerticalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }




