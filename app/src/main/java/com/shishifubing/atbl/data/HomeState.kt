package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

@Immutable
sealed interface HomeState {
    data class Success(
        val items: List<RowItems>,
        val settings: Model.Settings,
        val showHiddenApps: Boolean,
        val isHomeApp: Boolean,
        val appShortcutButtons: HomeDialogState.AppShortcutButtons
    ) : HomeState

    data object Loading : HomeState

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