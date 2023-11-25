package com.shishifubing.atbl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.R
import kotlinx.coroutines.delay

@Composable
fun BoxScope.LauncherPageIndicatorFloating(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    delayMillis: Long = 1000
) {
    var show by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        show = false
    }
    if (show) {
        LauncherPageIndicator(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .padding(0.dp, dimensionResource(R.dimen.padding_small)),
            curPage = currentPage,
            pageCount = pageCount
        )
    }
}

@Composable
private fun LauncherPageIndicator(
    curPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { i ->
                val color = if (curPage == i) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                )
            }
        }
    }

}

@Preview
@Composable
private fun LauncherPageIndicatorPreview() {
    LauncherPageIndicator(3, 10)
}