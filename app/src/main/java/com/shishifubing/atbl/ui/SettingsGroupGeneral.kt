package com.shishifubing.atbl.ui

import android.content.ContentResolver
import android.net.Uri
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
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UISettings
import java.io.FileOutputStream

@Composable
fun SettingsGroupGeneral(
    settings: UISettings,
    updateSettingsFromBytes: (ByteArray) -> Unit,
    backupReset: () -> Unit,
) {
    SettingsGroup(R.string.settings_group_general) {
        BackupExport(settings = settings)
        BackupImport(updateFromBytes = updateSettingsFromBytes)
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
private fun BackupImport(updateFromBytes: (ByteArray) -> Unit) {
    var result by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { result = it }
    SettingsField(
        name = R.string.settings_backup_import,
        label = stringResource(R.string.settings_backup_import_label),
        onClick = { launcher.launch(arrayOf("application/*")) }
    )
    LauncherEffectURI(uri = result) { uri ->
        openInputStream(uri)?.use { stream ->
            updateFromBytes(stream.readBytes())
            result = null
        }
    }
}

@Composable
private fun BackupExport(settings: UISettings) {
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
    LauncherEffectURI(uri = result) {
        openFileDescriptor(it, "w")?.use { file ->
            FileOutputStream(file.fileDescriptor).use { stream ->
                Model.Settings.newBuilder()
                    .setAppCard(settings.appCard.model)
                    .setLayout(settings.layout.model)
                    .build()
                    .writeTo(stream)
            }
        }
    }
}

@Composable
private fun LauncherEffectURI(
    uri: Uri?,
    action: ContentResolver.(uri: Uri) -> Unit
) {
    val resolver = LocalContext.current.contentResolver
    LaunchedEffect(key1 = uri) {
        if (uri != null) {
            resolver.apply { action(uri) }
        }
    }
}