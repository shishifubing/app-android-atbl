package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.Model.Settings
import com.shishifubing.atbl.Model.Settings.FontFamily.*
import com.shishifubing.atbl.Model.Settings.TextColor
import com.shishifubing.atbl.Model.Settings.TextStyle.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRowItemCard(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    settings: Settings.AppCard,
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
                style = getTextStyle(
                    settings.textStyle,
                    MaterialTheme.typography
                ),
                fontFamily = getFontFamily(settings.fontFamily),
                color = getTextColor(settings.textColor),
                text = label
            )
        }
    }
}

private fun getTextStyle(
    textStyle: Settings.TextStyle,
    typography: androidx.compose.material3.Typography
): TextStyle {
    return typography.let {
        when (textStyle) {
            DisplayLarge -> it.displayLarge
            DisplayMedium -> it.displayMedium
            DisplaySmall -> it.displaySmall
            HeadlineLarge -> it.headlineLarge
            HeadlineMedium -> it.headlineMedium
            HeadlineSmall -> it.headlineSmall
            TitleLarge -> it.titleLarge
            TitleMedium -> it.titleMedium
            TitleSmall -> it.titleSmall
            BodyLarge -> it.bodyLarge
            BodyMedium -> it.bodyMedium
            BodySmall -> it.bodySmall
            LabelLarge -> it.labelLarge
            LabelMedium -> it.labelMedium
            LabelSmall -> it.labelSmall
            else -> it.bodyMedium
        }
    }
}

private fun getTextColor(textColor: TextColor): Color {
    return when (textColor) {
        TextColor.Unspecified -> Color.Unspecified
        TextColor.Black -> Color.Black
        TextColor.DarkGray -> Color.DarkGray
        TextColor.Gray -> Color.Gray
        TextColor.LightGray -> Color.LightGray
        TextColor.White -> Color.White
        TextColor.Red -> Color.Red
        TextColor.Green -> Color.Green
        TextColor.Blue -> Color.Blue
        TextColor.Yellow -> Color.Yellow
        TextColor.Cyan -> Color.Cyan
        TextColor.Magenta -> Color.Magenta
        TextColor.Transparent -> Color.Transparent
        else -> Color.Unspecified
    }
}

private fun getFontFamily(fontFamily: Settings.FontFamily): FontFamily {
    return when (fontFamily) {
        Default -> FontFamily.Default
        Cursive -> FontFamily.Cursive
        Monospace -> FontFamily.Monospace
        SansSerif -> FontFamily.SansSerif
        Serif -> FontFamily.Serif
        else -> FontFamily.Default
    }
}

@Preview
@Composable
private fun HomeItemCardPreview() {
    LauncherTheme(darkTheme = true) {
        HomeRowItemCard(
            modifier = Modifier.padding(30.dp),
            label = "app",
            onClick = { /*TODO*/ },
            onLongClick = { /*TODO*/ },
            settings = Defaults.AppCardSettings
        )
    }
}