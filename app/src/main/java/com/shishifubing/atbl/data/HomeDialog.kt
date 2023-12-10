package com.shishifubing.atbl.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

@Immutable
sealed interface HomeDialogState {

    enum class HeaderActions {
        GoToInfo, HideOrShow, Uninstall
    }

    enum class LauncherDialogActions {
        GoToSettings, GoToAddWidget, GoToEditSplitScreenShortcuts,
        HideHiddenApps, ShowHiddenApps,
        AddScreenBefore, AddScreenAfter, RemoveScreen,
    }

    data class LauncherDialogState(
        val currentPage: Int,
        val pageCount: Int,
        val showHiddenApps: Boolean
    )

    data class AppShortcutButtons(val buttons: Map<String, Buttons<Model.AppShortcut>>)

    @Immutable
    sealed interface Header {

        data object None : Header

        data class App(val app: Model.App) : Header

        data class Shortcut(val shortcut: Model.SplitScreenShortcut) : Header
    }

    data class Buttons<T>(val buttons: List<Button<T>>) {
        constructor(vararg buttons: Button<T>) : this(buttons.toList())
    }

    data class Button<T>(val label: Label, val id: T) {
        constructor(label: String, id: T) : this(Label.Str(label), id)

        constructor(@StringRes label: Int, id: T) : this(Label.Res(label), id)

        @Immutable
        sealed interface Label {
            data class Str(val string: String) : Label

            data class Res(@StringRes val res: Int) : Label
        }
    }
}