package com.shishifubing.atbl.ui

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.R
import java.io.InputStream

@Composable
fun SettingsGroupGeneral(
    updateSettingsFromStream: (() -> InputStream?) -> Unit,
    writeSettingsToFile: (() -> ParcelFileDescriptor?) -> Unit,
    backupReset: () -> Unit,
) {
    SettingsGroup(R.string.settings_group_general) {
        BackupExport(writeSettingsToFile = writeSettingsToFile)
        BackupImport(updateSettingsFromStream = updateSettingsFromStream)
        BackupReset(resetSettings = backupReset)
    }
}

@Composable
private fun BackupReset(resetSettings: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    SettingsField(
        name = R.string.settings_backup_reset,
        label = stringResource(R.string.settings_backup_reset_label),
        onClick = { showDialog = true }
    )
    if (!showDialog) {
        return
    }
    SettingsDialog(
        name = R.string.settings_backup_reset,
        onConfirm = { resetSettings(); showDialog = false },
        onDismissRequest = { showDialog = false },
        itemsCount = 1,
        itemsKey = { 0 }
    ) {
        SettingsButton(
            text = stringResource(R.string.settings_backup_reset_confirmation),
            addButton = false
        )
    }
}

@Composable
private fun BackupImport(
    updateSettingsFromStream: (() -> InputStream?) -> Unit
) {
    var result by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { result = it }
    SettingsField(
        name = R.string.settings_backup_import,
        label = stringResource(R.string.settings_backup_import_label),
        onClick = { launcher.launch(arrayOf("application/*")) }
    )
    val contentResolver = LocalContext.current.contentResolver
    LaunchedEffect(result) {
        result?.let { uri ->
            updateSettingsFromStream { contentResolver.openInputStream(uri) }
            result = null
        }
    }
}

@Composable
private fun BackupExport(
    writeSettingsToFile: (() -> ParcelFileDescriptor?) -> Unit
) {
    var result by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/binpb")
    ) {
        result = it
    }
    val filename = stringResource(R.string.settings_backup_export_filename)
    SettingsField(
        name = R.string.settings_backup_export,
        label = stringResource(R.string.settings_backup_export_label),
        onClick = { launcher.launch(filename) }
    )
    val contentResolver = LocalContext.current.contentResolver
    LaunchedEffect(result) {
        result?.let { uri ->
            writeSettingsToFile {
                contentResolver.openFileDescriptor(uri, "w")
            }
            result = null
        }
    }
}