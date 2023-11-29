package com.shishifubing.atbl.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun HomeDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    actionButtons: List<Pair<String, () -> Unit>>? = null,
    header: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (header != null) {
                header()
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            }
            if (actionButtons.isNullOrEmpty()) {
                return@Column
            }
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
                        count = actionButtons.size,
                        key = { actionButtons[it].hashCode() },
                    ) {
                        val item = actionButtons[it]
                        HomeDialogButton(
                            text = item.first,
                            textAlign = TextAlign.Start,
                            onClick = {
                                item.second()
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
fun HomeDialogHeader(
    packageName: String,
    label: String,
    isHidden: Boolean,
    actions: AppActions,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon by remember { mutableStateOf(actions.getAppIcon(packageName)) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElevatedCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, dimensionResource(R.dimen.padding_medium)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .size(dimensionResource(R.dimen.image_size)),
                    bitmap = icon,
                    contentDescription = "App icon",
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        ElevatedCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HomeDialogButton(
                    text = stringResource(R.string.drawer_app_info),
                    onClick = {
                        actions.launchAppInfo(packageName)
                        onDismissRequest()
                    }
                )
                HomeDialogButton(
                    text = stringResource(
                        if (isHidden) R.string.drawer_app_show else R.string.drawer_app_hide
                    ),
                    onClick = {
                        actions.setIsHidden(packageName, !isHidden)
                        onDismissRequest()
                    }
                )
                HomeDialogButton(
                    text = stringResource(R.string.drawer_app_uninstall),
                    onClick = {
                        actions.launchAppUninstall(packageName)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeDialogButton(
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