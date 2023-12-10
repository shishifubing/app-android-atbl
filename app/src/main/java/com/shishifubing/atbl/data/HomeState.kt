package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

data class HomeState(
    val pages: Pages.Success,
    val settings: Model.Settings,
    val showHiddenApps: Boolean,
    val isHomeApp: Boolean,
    val appShortcutButtons: HomeDialogState.AppShortcutButtons
) {
    @Immutable
    sealed interface Pages {
        data object Loading : Pages
        data class Success(val items: List<RowItems>) : Pages
    }

    data class RowItems(val items: List<RowItem>)

    @Immutable
    sealed interface RowItem {
        val label: String

        data class App(
            val app: Model.App,
            override val label: String
        ) : RowItem

        data class SplitScreenShortcut(
            val shortcut: Model.SplitScreenShortcut,
            override val label: String
        ) : RowItem
    }
}