package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.LauncherStateRepository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemCard(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    settings: LauncherAppCardSettings,
    actions: AppActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .semantics { role = Role.Button }
            .clip(ButtonDefaults.textShape)
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = onClick
                )
                .defaultMinSize(
                    minWidth = ButtonDefaults.MinWidth,
                    minHeight = ButtonDefaults.MinHeight
                )
                .padding(ButtonDefaults.TextButtonContentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(settings.padding.dp),
                style = settings.textStyle.toTextStyle(),
                fontFamily = settings.fontFamily.toFontFamily(),
                color = settings.textColor.toColor(),
                text = actions.transformLabel(label, settings)
            )
        }
    }
}

@Preview
@Composable
private fun HomeItemCardPreview() {
    LauncherTheme(darkTheme = true) {
        HomeItemCard(
            modifier = Modifier.padding(30.dp),
            label = "app",
            onClick = { /*TODO*/ },
            onLongClick = { /*TODO*/ },
            settings = LauncherStateRepository.defaultSettings.appCardSettings(),
            actions = appActionStub
        )
    }
}