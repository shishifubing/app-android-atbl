package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Settings.HorizontalArrangement
import com.shishifubing.atbl.data.HomeState


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeRow(
    items: HomeState.RowItems,
    showHiddenApps: Boolean,
    onClick: (HomeState.RowItem) -> Unit,
    onLongClick: (HomeState.RowItem) -> Unit,
    settings: Model.Settings,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                settings.layout.horizontalPadding.dp,
                settings.layout.verticalPadding.dp
            ),
        horizontalArrangement = getArrangement(settings.layout.horizontalArrangement),
        verticalArrangement = getArrangement(settings.layout.verticalArrangement),
    ) {
        items.items.forEach {
            key(it.hashCode()) {
                val hide = it is HomeState.RowItem.App
                        && it.app.isHidden
                        && !showHiddenApps
                if (!hide) {
                    HomeRowItemCard(
                        label = it.label,
                        onClick = { onClick(it) },
                        onLongClick = { onLongClick(it) },
                        settings = settings.appCard
                    )
                }
            }
        }
    }
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

