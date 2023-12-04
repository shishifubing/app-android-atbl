package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Settings.HorizontalArrangement

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeRow(
    settings: Model.Settings.Layout,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                settings.horizontalPadding.dp,
                settings.verticalPadding.dp
            ),
        horizontalArrangement = getArrangement(settings.horizontalArrangement),
        verticalArrangement = getArrangement(settings.verticalArrangement),
        content = content
    )
}


private fun getArrangement(arrangement: HorizontalArrangement): Arrangement.Horizontal {
    return when (arrangement) {
        HorizontalArrangement.HorizontalStart -> Arrangement.Start
        HorizontalArrangement.HorizontalEnd -> Arrangement.End
        HorizontalArrangement.HorizontalCenter -> Arrangement.Center
        HorizontalArrangement.HorizontalSpaceEvenly -> Arrangement.SpaceEvenly
        HorizontalArrangement.HorizontalSpaceBetween -> Arrangement.SpaceBetween
        HorizontalArrangement.HorizontalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }
}

private fun getArrangement(arrangement: Model.Settings.VerticalArrangement): Arrangement.Vertical {
    return when (arrangement) {
        Model.Settings.VerticalArrangement.VerticalTop -> Arrangement.Top
        Model.Settings.VerticalArrangement.VerticalBottom -> Arrangement.Bottom
        Model.Settings.VerticalArrangement.VerticalCenter -> Arrangement.Center
        Model.Settings.VerticalArrangement.VerticalSpaceEvenly -> Arrangement.SpaceEvenly
        Model.Settings.VerticalArrangement.VerticalSpaceBetween -> Arrangement.SpaceBetween
        Model.Settings.VerticalArrangement.VerticalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }
}

