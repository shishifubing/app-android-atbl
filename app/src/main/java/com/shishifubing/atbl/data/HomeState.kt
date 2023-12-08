package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

@Immutable
sealed interface HomeState {
    @Immutable
    data class Success(
        val items: List<RowItems>,
        val settings: Model.Settings,
        val showHiddenApps: Boolean,
        val isHomeApp: Boolean,
        val appShortcutButtons: Map<String, HomeDialogButtonsState>
    ) : HomeState

    @Immutable
    data object Loading : HomeState

    @Immutable
    data class RowItems(val items: List<RowItem>)

    @Immutable
    sealed interface RowItem {
        val label: String

        @Immutable
        data class App(
            val app: Model.App,
            override val label: String
        ) : RowItem

        @Immutable
        data class SplitScreenShortcut(
            val shortcut: Model.SplitScreenShortcut,
            override val label: String
        ) : RowItem
    }
}