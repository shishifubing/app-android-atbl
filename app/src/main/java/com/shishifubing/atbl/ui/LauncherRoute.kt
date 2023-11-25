package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherRoute(
    navigate: (route: LauncherNav) -> Unit,
    modifier: Modifier = Modifier,
    vm: LauncherViewModel = viewModel(factory = LauncherViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    ErrorToast(errorFlow = vm.error)
    if (uiState !is LauncherUiState.Success) {
        return
    }
    val screens = (uiState as LauncherUiState.Success).screens
    val pagerState = rememberPagerState(pageCount = { screens.size })
    Box {
        HorizontalPager(modifier = modifier, state = pagerState) {
            LauncherScreen(
                modifier = Modifier.fillMaxSize(),
                navigate = navigate,
                screenState = screens[it],
                appActions = vm.appActions,
                splitScreenShortcutActions = vm.splitScreenShortcutActions
            )
        }
        LauncherPageIndicatorFloating(
            currentPage = pagerState.currentPage,
            pageCount = pagerState.pageCount
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LauncherScreen(
    navigate: (route: LauncherNav) -> Unit,
    modifier: Modifier = Modifier,
    screenState: LauncherScreenUiState,
    appActions: AppActions,
    splitScreenShortcutActions: SplitScreenShortcutActions
) {
    LauncherRow(
        modifier = modifier,
        rowSettings = screenState.launcherRowSettings,
        showHiddenAppsToggle = appActions::showHiddenAppsToggle,
        showHiddenApps = screenState.showHiddenApps,
        navigate = navigate
    ) {
        if (!screenState.isHomeApp) {
            NotAHomeAppBanner()
        }
        screenState.items.forEach {
            when (it) {
                is LauncherUiItem.Apps ->
                    LauncherApps(
                        apps = it.apps,
                        actions = appActions,
                        showShortcuts = screenState.isHomeApp,
                        launchShortcut = appActions::launchShortcut,
                        appCardSettings = screenState.appCardSettings
                    )

                is LauncherUiItem.Shortcuts ->
                    SplitScreenShortcuts(
                        shortcuts = it.shortcuts,
                        appCardSettings = screenState.appCardSettings,
                        appActions = appActions,
                        shortcutActions = splitScreenShortcutActions
                    )
            }
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





