package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    val showHiddenApps by vm.showHiddenApps.collectAsState()
    var showLauncherDialog by remember { mutableIntStateOf(-1) }
    var showAppDialog by remember { mutableStateOf<Model.App?>(null) }
    var showShortcutDialog by remember {
        mutableStateOf<Model.SplitScreenShortcut?>(null)
    }

    ErrorToast(errorFlow = vm.error)

    when (uiState) {
        HomeState.Loading -> {
            PageLoadingIndicator(
                modifier = modifier
            )
        }

        is HomeState.Success -> {
            val state = uiState as HomeState.Success
            val pagerState = rememberPagerState(
                initialPage = state.screens.size / 2,
                pageCount = { state.screens.size }
            )
            val interactionSource = remember { MutableInteractionSource() }
            Box(modifier = modifier) {
                HorizontalPager(state = pagerState) { page ->
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
                        onClick = {
                            when (it) {
                                is HomeState.RowItem.App -> {
                                    vm.launchApp(it.app)
                                }
                                is HomeState.RowItem.SplitScreenShortcut -> {
                                    vm.launchSplitScreenShortcut(it.shortcut)
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
                        getLabel =
                    )
                }
                HomePageIndicatorFloating(
                    currentPage = pagerState.currentPage,
                    pageCount = pagerState.pageCount
                )
            }
        }
    }
    if (showLauncherDialog != -1) {
        HomeLauncherDialogActions(
            navigate = navController::navigate,
            showHiddenApps = showHiddenApps,
            setShowHiddenApps = vm::setShowHiddenApps,
            addScreenAfter = vm::addScreenAfter,
            removeScreen = vm::removeScreen,
            addScreenBefore = vm::addScreenBefore,
            currentPage = showLauncherDialog,
            onDismissRequest = { showLauncherDialog = -1 }
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
    LauncherTheme(darkTheme = true) {
        HomePage(
            modifier = Modifier.fillMaxSize(),
            screenState = HomeState.ScreenState(
                isHomeApp = true,
                settings = Defaults.Settings,
                items = listOf(
                    HomeState.ItemState.Apps(
                        state = HomeItemAppsState(
                            apps = apps,
                            showHiddenApps = false,
                            showShortcuts = true,
                            settings = Defaults.AppCardSettings,
                            setIsHidden = { _, _ -> },
                            launchAppUninstall = {},
                            launchAppInfo = {},
                            transformLabel = { txt, _ -> txt },
                            launchShortcut = {},
                            launchApp = {},
                        )
                    ),
                    HomeState.ItemState.Shortcuts(
                        state = HomeItemSplitScreenShortcutsState(
                            shortcuts = shortcuts,
                            launchAppInfo = {},
                            launchAppUninstall = {},
                            setIsHidden = { _, _ -> },
                            settings = Defaults.AppCardSettings,
                            getLabel = { shortcut, _ -> shortcut.key },
                            launchSplitScreenShortcut = {},
                            removeSplitScreenShortcut = {}
                        )
                    )
                )
            )
        )
    }
}




