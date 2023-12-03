package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R

@Composable
fun SettingsGroupHiddenApps(
    apps: Model.Apps,
    setHiddenApps: (List<String>) -> Unit
) {
    SettingsGroup(R.string.settings_group_hidden_apps) {
        HiddenApps(
            apps = apps,
            setHiddenApps = setHiddenApps
        )
    }
}

@Composable
private fun HiddenApps(
    apps: Model.Apps,
    setHiddenApps: (List<String>) -> Unit
) {
    val launcherPackageName = LocalContext.current.packageName
    val options = apps.appsMap.values
        .filter { it.packageName != launcherPackageName }
        .sortedBy { it.label }
    var hiddenApps =
        options.mapIndexedNotNull { i, app -> if (app.isHidden) i else null }

    SettingsFieldMultiChoice(
        name = R.string.settings_hidden_apps,
        selectedOptions = hiddenApps,
        options = options.map { it.label },
        onConfirm = { choices ->
            setHiddenApps(choices.map { options[it].packageName })
            hiddenApps = choices
        }
    )
}