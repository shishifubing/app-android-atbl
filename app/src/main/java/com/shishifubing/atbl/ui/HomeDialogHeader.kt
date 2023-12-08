package com.shishifubing.atbl.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R

@Composable
fun HomeDialogHeader(
    app: Model.App,
    launchAppInfo: (Model.App) -> Unit,
    launchAppUninstall: (Model.App) -> Unit,
    setIsHidden: (Model.App, Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = remember {
        val array = app.icon.toByteArray()
        BitmapFactory.decodeByteArray(array, 0, array.size).asImageBitmap()
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElevatedCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, dimensionResource(R.dimen.padding_medium)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .padding(dimensionResource(R.dimen.padding_small))
                        .size(dimensionResource(R.dimen.image_size)),
                    bitmap = icon,
                    contentDescription = "App icon",
                )
                Text(
                    text = app.label,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
        ElevatedCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HomeDialogButton(
                    text = stringResource(R.string.drawer_app_info),
                    onClick = {
                        launchAppInfo(app)
                        onDismissRequest()
                    }
                )
                HomeDialogButton(
                    text = stringResource(
                        if (app.isHidden) R.string.drawer_app_show else R.string.drawer_app_hide
                    ),
                    onClick = {
                        setIsHidden(app, !app.isHidden)
                        onDismissRequest()
                    }
                )
                HomeDialogButton(
                    text = stringResource(R.string.drawer_app_uninstall),
                    onClick = {
                        launchAppUninstall(app)
                        onDismissRequest()
                    }
                )
            }
        }
    }
}