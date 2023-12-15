package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

data class HomeState(
    val items: RowItems,
    val state: Model.State
) {
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