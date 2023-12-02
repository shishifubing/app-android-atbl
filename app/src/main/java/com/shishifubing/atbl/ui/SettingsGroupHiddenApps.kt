package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UIApps

@Composable
fun SettingsGroupHiddenApps(
    apps: UIApps,
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
    apps: UIApps,
    setHiddenApps: (List<String>) -> Unit
) {
    val launcherPackageName = LocalContext.current.packageName
    val options = apps.model
        .filter { it.model.packageName != launcherPackageName }
        .sortedBy { it.model.label }
    var hiddenApps =
        options.mapIndexedNotNull { i, app -> if (app.model.isHidden) i else null }

    SettingsFieldMultiChoice(
        name = R.string.settings_hidden_apps,
        selectedOptions = hiddenApps,
        options = options.map { it.model.label },
        onConfirm = { choices ->
            setHiddenApps(choices.map { options[it].model.packageName })
            hiddenApps = choices
        }
    )
}