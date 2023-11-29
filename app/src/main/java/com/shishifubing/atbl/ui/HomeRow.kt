package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeRow(
    rowSettings: LauncherRowSettings,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                rowSettings.horizontalPadding.dp,
                rowSettings.verticalPadding.dp
            ),
        horizontalArrangement = rowSettings.horizontalArrangement.toArrangement(),
        verticalArrangement = rowSettings.verticalArrangement.toArrangement(),
        content = content
    )
}


