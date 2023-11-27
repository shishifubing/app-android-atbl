package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun LauncherScaffold(
    route: String?,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (route != null && route != LauncherNav.Home.name) {
                LauncherTopBar(label = route, goBack = goBack)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LauncherTopBar(label: String, goBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = goBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Arrow back"
                )
            }
        }
    )
}

@Preview
@Composable
private fun LauncherScaffoldPreview() {
    LauncherTheme(darkTheme = true) {
        LauncherScaffold("Settings", {}) {
            Column {
                Text("content1", Modifier.padding(30.dp))
                Text("content2", Modifier.padding(30.dp))
            }
        }
    }
}

@Preview
@Composable
private fun LauncherScaffoldHomePreview() {
    LauncherTheme(darkTheme = true) {
        LauncherScaffold("Settings", {}) {
            Column {
                Text("content1", Modifier.padding(30.dp))
                Text("content2", Modifier.padding(30.dp))
            }
        }
    }
}