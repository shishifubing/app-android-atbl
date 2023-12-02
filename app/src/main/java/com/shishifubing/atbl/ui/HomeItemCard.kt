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
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.UISettingsAppCard
import com.shishifubing.atbl.data.defaultUISettingsAppCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemCard(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    transformLabel: (String, Model.Settings.AppCard) -> String,
    settings: UISettingsAppCard,
    modifier: Modifier = Modifier,
) {
    val model = settings.model
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
                modifier = Modifier.padding(model.padding.dp),
                style = model.textStyle.toTextStyle(),
                fontFamily = model.fontFamily.toFontFamily(),
                color = model.textColor.toColor(),
                text = transformLabel(label, settings.model)
            )
        }
    }
}

@Composable
private fun Model.Settings.TextStyle.toTextStyle(): TextStyle {
    return MaterialTheme.typography.let {
        when (this) {
            Model.Settings.TextStyle.DisplayLarge -> it.displayLarge
            Model.Settings.TextStyle.DisplayMedium -> it.displayMedium
            Model.Settings.TextStyle.DisplaySmall -> it.displaySmall
            Model.Settings.TextStyle.HeadlineLarge -> it.headlineLarge
            Model.Settings.TextStyle.HeadlineMedium -> it.headlineMedium
            Model.Settings.TextStyle.HeadlineSmall -> it.headlineSmall
            Model.Settings.TextStyle.TitleLarge -> it.titleLarge
            Model.Settings.TextStyle.TitleMedium -> it.titleMedium
            Model.Settings.TextStyle.TitleSmall -> it.titleSmall
            Model.Settings.TextStyle.BodyLarge -> it.bodyLarge
            Model.Settings.TextStyle.BodyMedium -> it.bodyMedium
            Model.Settings.TextStyle.BodySmall -> it.bodySmall
            Model.Settings.TextStyle.LabelLarge -> it.labelLarge
            Model.Settings.TextStyle.LabelMedium -> it.labelMedium
            Model.Settings.TextStyle.LabelSmall -> it.labelSmall
            else -> it.bodyMedium
        }
    }
}

@Composable
private fun Model.Settings.TextColor.toColor(): Color {
    return when (this) {
        Model.Settings.TextColor.Unspecified -> Color.Unspecified
        Model.Settings.TextColor.Black -> Color.Black
        Model.Settings.TextColor.DarkGray -> Color.DarkGray
        Model.Settings.TextColor.Gray -> Color.Gray
        Model.Settings.TextColor.LightGray -> Color.LightGray
        Model.Settings.TextColor.White -> Color.White
        Model.Settings.TextColor.Red -> Color.Red
        Model.Settings.TextColor.Green -> Color.Green
        Model.Settings.TextColor.Blue -> Color.Blue
        Model.Settings.TextColor.Yellow -> Color.Yellow
        Model.Settings.TextColor.Cyan -> Color.Cyan
        Model.Settings.TextColor.Magenta -> Color.Magenta
        Model.Settings.TextColor.Transparent -> Color.Transparent
        else -> Color.Unspecified
    }
}


@Composable
private fun Model.Settings.FontFamily.toFontFamily(): FontFamily {
    return when (this) {
        Model.Settings.FontFamily.Default -> FontFamily.Default
        Model.Settings.FontFamily.Cursive -> FontFamily.Cursive
        Model.Settings.FontFamily.Monospace -> FontFamily.Monospace
        Model.Settings.FontFamily.SansSerif -> FontFamily.SansSerif
        Model.Settings.FontFamily.Serif -> FontFamily.Serif
        else -> FontFamily.Default
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
            settings = defaultUISettingsAppCard,
            transformLabel = { label, _ -> label }
        )
    }
}