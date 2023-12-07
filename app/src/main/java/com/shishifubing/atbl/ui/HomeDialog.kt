package com.shishifubing.atbl.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shishifubing.atbl.R

@Immutable
data class HomeDialogButtons(
    val buttons: List<HomeDialogButton>
) {
    constructor(button: HomeDialogButton) : this(listOf(button))
}

@Immutable
data class HomeDialogButton(
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

@Immutable
data class HomeDialogHeaders(
    val headers: List<@Composable () -> Unit>
) {
    constructor(header: @Composable () -> Unit) : this(listOf(header))
}

@Composable
fun HomeDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showButtons: Boolean = true,
    actionButtons: HomeDialogButtons = HomeDialogButtons(listOf()),
    headers: HomeDialogHeaders = HomeDialogHeaders(listOf()),
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            headers.headers.forEach { header ->
                key(header.hashCode()) {
                    header()
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            }
            if (actionButtons.buttons.isNotEmpty() && showButtons) {
                ElevatedCard {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                0.dp,
                                (LocalConfiguration.current.screenHeightDp * 0.6).dp
                            )
                    ) {
                        items(
                            count = actionButtons.buttons.size,
                            key = { actionButtons.buttons[it].hashCode() },
                        ) {
                            val item = actionButtons.buttons[it]
                            if (item.show) {
                                HomeDialogButton(
                                    text = when (item.label) {
                                        is HomeDialogButton.Label.Comp ->
                                            item.label.getLabel()

                                        is HomeDialogButton.Label.Res ->
                                            stringResource(item.label.res)

                                        is HomeDialogButton.Label.Str ->
                                            item.label.string
                                    },
                                    textAlign = TextAlign.Start,
                                    onClick = {
                                        item.onClick()
                                        onDismissRequest()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = textAlign
        )
    }
}

@Preview
@Composable
private fun HomeDialogButtonPreview() {
    LauncherTheme {
        HomeDialogButton(text = "button", onClick = { })
    }
}