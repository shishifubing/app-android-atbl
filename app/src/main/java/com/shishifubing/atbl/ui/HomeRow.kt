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
import com.shishifubing.atbl.data.UISettingsLayout

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeRow(
    settings: UISettingsLayout,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    val model = settings.model
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(model.horizontalPadding.dp, model.verticalPadding.dp),
        horizontalArrangement = model.horizontalArrangement.toArrangement(),
        verticalArrangement = model.verticalArrangement.toArrangement(),
        content = content
    )
}


private fun Model.Settings.HorizontalArrangement.toArrangement(): Arrangement.Horizontal =
    when (this) {
        Model.Settings.HorizontalArrangement.HorizontalStart -> Arrangement.Start
        Model.Settings.HorizontalArrangement.HorizontalEnd -> Arrangement.End
        Model.Settings.HorizontalArrangement.HorizontalCenter -> Arrangement.Center
        Model.Settings.HorizontalArrangement.HorizontalSpaceEvenly -> Arrangement.SpaceEvenly
        Model.Settings.HorizontalArrangement.HorizontalSpaceBetween -> Arrangement.SpaceBetween
        Model.Settings.HorizontalArrangement.HorizontalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }

private fun Model.Settings.VerticalArrangement.toArrangement(): Arrangement.Vertical =
    when (this) {
        Model.Settings.VerticalArrangement.VerticalTop -> Arrangement.Top
        Model.Settings.VerticalArrangement.VerticalBottom -> Arrangement.Bottom
        Model.Settings.VerticalArrangement.VerticalCenter -> Arrangement.Center
        Model.Settings.VerticalArrangement.VerticalSpaceEvenly -> Arrangement.SpaceEvenly
        Model.Settings.VerticalArrangement.VerticalSpaceBetween -> Arrangement.SpaceBetween
        Model.Settings.VerticalArrangement.VerticalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }

