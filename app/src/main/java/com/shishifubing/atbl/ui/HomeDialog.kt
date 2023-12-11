package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.HomeDialogState
import com.shishifubing.atbl.data.HomeDialogState.Button.Label
import com.shishifubing.atbl.data.HomeDialogState.Header

@Composable
fun <T> HomeDialog(
    onDismissRequest: () -> Unit,
    onButtonClick: (T) -> Unit,
    actionButtons: HomeDialogState.Buttons<T>,
    modifier: Modifier = Modifier,
    showButton: (T) -> Boolean = { true },
) {
    HomeDialogBase(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        HomeDialogButtons(
            onDismissRequest = onDismissRequest,
            onButtonClick = onButtonClick,
            showButton = showButton,
            actionButtons = actionButtons,
        )
    }
}

@Composable
fun <T> HomeDialog(
    onDismissRequest: () -> Unit,
    onButtonClick: (T) -> Unit,
    actionButtons: HomeDialogState.Buttons<T>,
    header: Header,
    onHeaderAction: (Model.App, HomeDialogState.HeaderActions) -> Unit,
    modifier: Modifier = Modifier,
    showButton: (T) -> Boolean = { true },
    showButtons: Boolean = true,
) {
    HomeDialogBase(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        HomeDialogHeaders(
            header = header,
            onHeaderAction = onHeaderAction,
            onDismissRequest = onDismissRequest
        )
        if (actionButtons.buttons.isNotEmpty() && showButtons) {
            HomeDialogButtons(
                onDismissRequest = onDismissRequest,
                onButtonClick = onButtonClick,
                showButton = showButton,
                actionButtons = actionButtons,
            )
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

@Composable
private fun <T> HomeDialogButtons(
    onDismissRequest: () -> Unit,
    onButtonClick: (T) -> Unit,
    showButton: (T) -> Boolean,
    actionButtons: HomeDialogState.Buttons<T>,
) {
    ElevatedCard {
        Column {
            actionButtons.buttons.forEach { button ->
                key(button.hashCode()) {
                    if (showButton(button.id)) {
                        HomeDialogButton(
                            text = when (button.label) {
                                is Label.Res -> stringResource(button.label.res)
                                is Label.Str -> button.label.string
                            },
                            textAlign = TextAlign.Start,
                            onClick = {
                                onButtonClick(button.id)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDialogHeaders(
    header: Header,
    onDismissRequest: () -> Unit,
    onHeaderAction: (Model.App, HomeDialogState.HeaderActions) -> Unit,
) {
    when (header) {
        is Header.None -> Unit

        is Header.App -> {
            HomeDialogHeader(
                app = header.app,
                onHeaderAction = onHeaderAction,
                onDismissRequest = onDismissRequest
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        }

        is Header.Shortcut -> {
            HomeDialogHeader(
                app = header.shortcut.appSecond,
                onHeaderAction = onHeaderAction,
                onDismissRequest = onDismissRequest
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            HomeDialogHeader(
                app = header.shortcut.appFirst,
                onHeaderAction = onHeaderAction,
                onDismissRequest = onDismissRequest
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        }
    }
}

@Composable
private fun HomeDialogBase(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
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