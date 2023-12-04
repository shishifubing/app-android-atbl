package com.shishifubing.atbl.ui

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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UIModel

typealias HomeDialogButtons = UIModel<List<Pair<String, () -> Unit>>>
typealias HomeDialogHeaders = UIModel<List<@Composable () -> Unit>>

@Composable
fun HomeDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
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
            headers.model.forEach { header ->
                key(header.hashCode()) {
                    header()
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            }
            if (actionButtons.model.isEmpty()) {
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
                        count = actionButtons.model.size,
                        key = { actionButtons.model[it].hashCode() },
                    ) {
                        val item = actionButtons.model[it]
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