package com.shishifubing.atbl.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.R
import kotlinx.coroutines.delay

@Composable
fun BoxScope.HomePageIndicatorFloating(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    delayMillis: Long = 1000
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(currentPage, pageCount) {
        show = true
        delay(delayMillis)
        show = false
    }
    if (show) {
        HomePageIndicator(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .padding(0.dp, dimensionResource(R.dimen.padding_medium)),
            curPage = currentPage,
            pageCount = pageCount
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomePageIndicator(
    curPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.inversePrimary)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { i ->
                val color = if (curPage == i) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.inversePrimary
                }
                Box(
                    modifier = Modifier
                        .background(color)
                        .size(dimensionResource(R.dimen.padding_medium))
                        .padding(dimensionResource(R.dimen.padding_small))
                )
            }
        }
    }

}

@Preview
@Composable
private fun HomePageIndicatorPreview() {
    LauncherTheme(darkTheme = true) {
        HomePageIndicator(3, 10)
    }
}

@Preview
@Composable
private fun HomePageIndicatorFloatingPreview() {
    LauncherTheme(darkTheme = true) {
        Box(Modifier.size(300.dp)) {
            HomePageIndicatorFloating(3, 10)
        }
    }
}

@Preview
@Composable
private fun HomePageIndicatorFloatingOverflowPreview() {
    LauncherTheme(darkTheme = true) {
        Box(Modifier.size(300.dp)) {
            HomePageIndicatorFloating(3, 30)
        }
    }
}