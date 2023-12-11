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
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.HomeDialogState
import com.shishifubing.atbl.data.HomeDialogState.HeaderActions
import com.shishifubing.atbl.data.HomeState
import com.shishifubing.atbl.data.HomeState.RowItem
import com.shishifubing.atbl.data.UiState

object HomeRoute : LauncherRoute<HomeState, HomeViewModel> {
    override val url = "home_screen"
    override val label = R.string.navigation_home
    override val showScaffold = false

    @Composable
    override fun getViewModel(): HomeViewModel {
        return viewModel(factory = HomeViewModel.Factory)
    }

    @Composable
    override fun Content(
        vm: HomeViewModel,
        uiState: UiState.Success<HomeState>
    ) {
        Box(modifier = Modifier.safeDrawingPadding()) {
            HomeScreen(
                state = uiState.state,
                onSplitScreenShortcutsDialogClick = vm::onSplitScreenShortcutsDialogClick,
                onHeaderAction = vm::onHeaderAction,
                onAppDialogClick = vm::onAppDialogClick,
                onRowItemClick = vm::onRowItemClick,
                onLauncherDialogAction = vm::onLauncherDialogAction
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.HomeScreen(
    state: HomeState,
    onAppDialogClick: (Model.AppShortcut) -> Unit,
    onSplitScreenShortcutsDialogClick: (Model.SplitScreenShortcut) -> Unit,
    onLauncherDialogAction: (
        HomeDialogState.LauncherDialogState,
        HomeDialogState.LauncherDialogActions
    ) -> Unit,
    onRowItemClick: (RowItem) -> Unit,
    onHeaderAction: (Model.App, HeaderActions) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLauncherDialog by remember { mutableIntStateOf(-1) }
    var showAppDialog by remember { mutableStateOf<Model.App?>(null) }
    var showShortcutDialog by remember {
        mutableStateOf<Model.SplitScreenShortcut?>(null)
    }
    val pagerState = rememberPagerState(
        initialPage = state.pages.size / 2,
        pageCount = { state.pages.size }
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
                items = state.pages[page],
                showHiddenApps = state.showHiddenApps,
                onClick = onRowItemClick,
                onLongClick = {
                    when (it) {
                        is RowItem.App -> showAppDialog = it.app

                        is RowItem.SplitScreenShortcut -> {
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
            state = HomeDialogState.LauncherDialogState(
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
                showHiddenApps = state.showHiddenApps
            ),
            onLauncherDialogAction = onLauncherDialogAction,
            onDismissRequest = { showLauncherDialog = -1 }
        )
    }
    showAppDialog?.let {
        HomeDialogApp(
            app = it,
            allShortcuts = state.appShortcutButtons,
            showShortcuts = state.isHomeApp,
            onAppShortcutClick = onAppDialogClick,
            onHeaderAction = onHeaderAction,
            onDismissRequest = { showAppDialog = null }
        )
    }
    showShortcutDialog?.let {
        HomeDialogSplitScreenShortcut(
            shortcut = it,
            onSplitScreenShortcutsDialogClick = onSplitScreenShortcutsDialogClick,
            onHeaderAction = onHeaderAction,
            onDismissRequest = { showShortcutDialog = null }
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
    val state = HomeState(
        showHiddenApps = false,
        settings = Defaults.Settings,
        isHomeApp = true,
        appShortcutButtons = HomeDialogState.AppShortcutButtons(mapOf()),
        pages = listOf()
    )
    LauncherTheme(darkTheme = true) {
        Box {
            HomeScreen(
                modifier = Modifier,
                state = state,
                onSplitScreenShortcutsDialogClick = {},
                onLauncherDialogAction = { _, _ -> },
                onAppDialogClick = {},
                onHeaderAction = { _, _ -> },
                onRowItemClick = {}
            )
        }
    }
}




