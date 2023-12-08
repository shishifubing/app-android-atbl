package com.shishifubing.atbl.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
data class HomeDialogHeaders(
    val headers: List<@Composable () -> Unit>
) {
    constructor(header: @Composable () -> Unit) : this(listOf(header))
}

@Immutable
data class HomeDialogButtonsState(
    val buttons: List<HomeDialogButtonState>
) {
    constructor(button: HomeDialogButtonState) : this(listOf(button))
}

@Immutable
data class HomeDialogButtonState(
    val label: Label,
    val show: Boolean = true,
    val onClick: () -> Unit
) {
    constructor(
        label: String,
        show: Boolean = true,
        onClick: () -> Unit
    ) : this(Label.Str(label), show, onClick)

    constructor(
        @StringRes label: Int,
        show: Boolean = true,
        onClick: () -> Unit
    ) : this(Label.Res(label), show, onClick)

    constructor(
        label: @Composable () -> String,
        show: Boolean = true,
        onClick: () -> Unit
    ) : this(Label.Comp(getLabel = label), show, onClick)

    @Immutable
    sealed interface Label {
        @Immutable
        data class Str(val string: String) : Label

        @Immutable
        data class Res(@StringRes val res: Int) : Label

        @Immutable
        data class Comp(val getLabel: @Composable () -> String) : Label
    }
}