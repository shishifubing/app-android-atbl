package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement

inline fun <reified E : Enum<E>> enumToList(): List<String> {
    return enumValues<E>()
        .filterNot { it.name == "UNRECOGNIZED" }
        .map { it.name }
}

@Composable
fun LauncherTextStyle.toTextStyle(): TextStyle = MaterialTheme.typography.let {
    when (this) {
        LauncherTextStyle.DisplayLarge -> it.displayLarge
        LauncherTextStyle.DisplayMedium -> it.displayMedium
        LauncherTextStyle.DisplaySmall -> it.displaySmall
        LauncherTextStyle.HeadlineLarge -> it.headlineLarge
        LauncherTextStyle.HeadlineMedium -> it.headlineMedium
        LauncherTextStyle.HeadlineSmall -> it.headlineSmall
        LauncherTextStyle.TitleLarge -> it.titleLarge
        LauncherTextStyle.TitleMedium -> it.titleMedium
        LauncherTextStyle.TitleSmall -> it.titleSmall
        LauncherTextStyle.BodyLarge -> it.bodyLarge
        LauncherTextStyle.BodyMedium -> it.bodyMedium
        LauncherTextStyle.BodySmall -> it.bodySmall
        LauncherTextStyle.LabelLarge -> it.labelLarge
        LauncherTextStyle.LabelMedium -> it.labelMedium
        LauncherTextStyle.LabelSmall -> it.labelSmall
        else -> it.bodyMedium
    }
}

@Composable
fun LauncherTextColor.toColor(): Color = when (this) {
    LauncherTextColor.Unspecified -> Color.Unspecified
    LauncherTextColor.Black -> Color.Black
    LauncherTextColor.DarkGray -> Color.DarkGray
    LauncherTextColor.Gray -> Color.Gray
    LauncherTextColor.LightGray -> Color.LightGray
    LauncherTextColor.White -> Color.White
    LauncherTextColor.Red -> Color.Red
    LauncherTextColor.Green -> Color.Green
    LauncherTextColor.Blue -> Color.Blue
    LauncherTextColor.Yellow -> Color.Yellow
    LauncherTextColor.Cyan -> Color.Cyan
    LauncherTextColor.Magenta -> Color.Magenta
    LauncherTextColor.Transparent -> Color.Transparent
    else -> Color.Unspecified
}


@Composable
fun LauncherFontFamily.toFontFamily(): FontFamily = when (this) {
    LauncherFontFamily.Default -> FontFamily.Default
    LauncherFontFamily.Cursive -> FontFamily.Cursive
    LauncherFontFamily.Monospace -> FontFamily.Monospace
    LauncherFontFamily.SansSerif -> FontFamily.SansSerif
    LauncherFontFamily.Serif -> FontFamily.Serif
    else -> FontFamily.Default
}

@Composable
fun LauncherHorizontalArrangement.toArrangement(): Arrangement.Horizontal =
    when (this) {
        LauncherHorizontalArrangement.HorizontalStart -> Arrangement.Start
        LauncherHorizontalArrangement.HorizontalEnd -> Arrangement.End
        LauncherHorizontalArrangement.HorizontalCenter -> Arrangement.Center
        LauncherHorizontalArrangement.HorizontalSpaceEvenly -> Arrangement.SpaceEvenly
        LauncherHorizontalArrangement.HorizontalSpaceBetween -> Arrangement.SpaceBetween
        LauncherHorizontalArrangement.HorizontalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }

@Composable
fun LauncherVerticalArrangement.toArrangement(): Arrangement.Vertical =
    when (this) {
        LauncherVerticalArrangement.VerticalTop -> Arrangement.Top
        LauncherVerticalArrangement.VerticalBottom -> Arrangement.Bottom
        LauncherVerticalArrangement.VerticalCenter -> Arrangement.Center
        LauncherVerticalArrangement.VerticalSpaceEvenly -> Arrangement.SpaceEvenly
        LauncherVerticalArrangement.VerticalSpaceBetween -> Arrangement.SpaceBetween
        LauncherVerticalArrangement.VerticalSpaceAround -> Arrangement.SpaceAround
        else -> Arrangement.Center
    }