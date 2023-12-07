package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R

@Composable
fun HomeRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()

    ErrorToast(errorFlow = vm.errorFlow)

    when (uiState) {
        HomeState.Loading -> {
            PageLoadingIndicator(
                modifier = modifier
            )
        }

        is HomeState.Success -> {
            Box {
                HomeScreen(
                    modifier = modifier,
                    state = uiState as HomeState.Success,
                    navigate = navController::navigate,
                    launchApp = vm::launchApp,
                    launchSplitScreenShortcut = vm::launchSplitScreenShortcut,
                    removeSplitScreenShortcut = vm::removeSplitScreenShortcut,
                    setShowHiddenApps = vm::setShowHiddenApps,
                    addScreenBefore = vm::addScreenBefore,
                    addScreenAfter = vm::addScreenAfter,
                    removeScreen = vm::removeScreen,
                    launchAppInfo = vm::launchAppInfo,
                    launchAppUninstall = vm::launchAppUninstall,
                    setIsHidden = vm::setIsHidden,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.HomeScreen(
    state: HomeState.Success,
    navigate: (LauncherNav) -> Unit,
    launchApp: (Model.App) -> Unit,
    launchSplitScreenShortcut: (Model.SplitScreenShortcut) -> Unit,
    removeSplitScreenShortcut: (Model.SplitScreenShortcut) -> Unit,
    setShowHiddenApps: (Boolean) -> Unit,
    addScreenBefore: (Int) -> Unit,
    addScreenAfter: (Int) -> Unit,
    removeScreen: (Int) -> Unit,
    launchAppInfo: (Model.App) -> Unit,
    launchAppUninstall: (Model.App) -> Unit,
    setIsHidden: (Model.App, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLauncherDialog by remember { mutableIntStateOf(-1) }
    var showAppDialog by remember { mutableStateOf<Model.App?>(null) }
    var showShortcutDialog by remember {
        mutableStateOf<Model.SplitScreenShortcut?>(null)
    }
    val pagerState = rememberPagerState(
        initialPage = state.items.size / 2,
        pageCount = { state.items.size }
    )
    val interactionSource = remember { MutableInteractionSource() }
    Column {
        if (!state.isHomeApp) {
            NotAHomeAppBanner()
        }
        HorizontalPager(state = pagerState, modifier = modifier) { page ->
            HomeRow(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onLongClick = { showLauncherDialog = page },
                        onClick = { }
                    ),
                settings = state.settings,
                items = state.items[page],
                showHiddenApps = state.showHiddenApps,
                onClick = {
                    when (it) {
                        is HomeState.RowItem.App -> {
                            launchApp(it.app)
                        }

                        is HomeState.RowItem.SplitScreenShortcut -> {
                            launchSplitScreenShortcut(it.shortcut)
                        }
                    }
                },
                onLongClick = {
                    when (it) {
                        is HomeState.RowItem.App -> {
                            showAppDialog = it.app
                        }

                        is HomeState.RowItem.SplitScreenShortcut -> {
                            showShortcutDialog = it.shortcut
                        }
                    }
                },
            )
        }
    }
    HomePageIndicatorFloating(
        currentPage = pagerState.currentPage,
        pageCount = pagerState.pageCount
    )
    if (showLauncherDialog != -1) {
        HomeDialogLauncherActions(
            navigate = navigate,
            showHiddenApps = state.showHiddenApps,
            setShowHiddenApps = setShowHiddenApps,
            addScreenAfter = addScreenAfter,
            removeScreen = removeScreen,
            addScreenBefore = addScreenBefore,
            currentPage = showLauncherDialog,
            pageCount = pagerState.pageCount,
            onDismissRequest = { showLauncherDialog = -1 }
        )
    }
    if (showAppDialog != null) {
        val app = showAppDialog!!
        HomeDialog(
            onDismissRequest = { showAppDialog = null },
            actionButtons = state.appShortcutButtons[app.packageName]!!,
            headers = HomeDialogHeaders {
                HomeDialogHeader(
                    app = app,
                    onDismissRequest = { showAppDialog = null },
                    launchAppInfo = launchAppInfo,
                    launchAppUninstall = launchAppUninstall,
                    setIsHidden = setIsHidden
                )
            }
        )
    }
    if (showShortcutDialog != null) {
        val shortcut = showShortcutDialog!!
        HomeDialog(
            onDismissRequest = { showShortcutDialog = null },
            modifier = modifier,
            actionButtons = HomeDialogButtons(
                HomeDialogButton(
                    label = R.string.drawer_app_delete_split_screen_shortcut,
                    onClick = { removeSplitScreenShortcut(shortcut) }
                )
            ),
            headers = HomeDialogHeaders(listOf(
                {
                    HomeDialogHeader(
                        app = shortcut.appSecond,
                        launchAppInfo = launchAppInfo,
                        launchAppUninstall = launchAppUninstall,
                        setIsHidden = setIsHidden,
                        onDismissRequest = {
                            showShortcutDialog = null
                        }
                    )
                },
                {
                    HomeDialogHeader(
                        app = shortcut.appFirst,
                        launchAppInfo = launchAppInfo,
                        launchAppUninstall = launchAppUninstall,
                        setIsHidden = setIsHidden,
                        onDismissRequest = {
                            showShortcutDialog = null
                        }
                    )
                }
            ))
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
        Model.App.getDefaultInstance()
            .toBuilder()
            .setLabel("app-$it")
            .build()
    }
    val shortcuts = listOf(
        Model.SplitScreenShortcut.getDefaultInstance()
            .toBuilder()
            .setAppFirst(apps[0])
            .setAppSecond(apps[1])
            .build()
    )
    val state = HomeState.Success(
        showHiddenApps = false,
        settings = Defaults.Settings,
        isHomeApp = true,
        appShortcutButtons = mapOf(),
        items = listOf()
    )
    LauncherTheme(darkTheme = true) {
        Box {
            HomeScreen(
                modifier = Modifier,
                state = state,
                navigate = {},
                launchApp = { },
                launchSplitScreenShortcut = { },
                removeSplitScreenShortcut = {},
                setShowHiddenApps = {},
                addScreenBefore = {},
                addScreenAfter = {},
                removeScreen = {},
                launchAppInfo = {},
                launchAppUninstall = {},
                setIsHidden = { _, _ -> },
            )
        }
    }
}




