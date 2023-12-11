package com.shishifubing.atbl.ui


import android.os.ParcelFileDescriptor
import com.shishifubing.atbl.LauncherNavigator
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.launcherViewModelFactory
import java.io.FileOutputStream
import java.io.InputStream


class SettingsViewModel(
    private val stateRepo: LauncherStateRepository,
    private val navigator: LauncherNavigator,
) : StateViewModel(stateRepo, navigator) {
    companion object {
        val Factory = launcherViewModelFactory {
            SettingsViewModel(stateRepo = stateRepo, navigator = navigator)
        }
    }

    fun backupReset() {
        launch { stateRepo.resetSettings() }
    }

    fun setHiddenApps(packageNames: List<String>) {
        stateAction { setHiddenApps(packageNames) }
    }

    fun setAppLayoutReverseOrder(value: Boolean) {
        updateLayout { reverseOrder = value }
    }

    fun setAppLayoutHorizontalPadding(value: Int) {
        updateLayout { horizontalPadding = value }
    }

    fun setAppLayoutVerticalPadding(value: Int) {
        updateLayout { verticalPadding = value }
    }

    fun setAppLayoutHorizontalArrangement(value: Model.Settings.HorizontalArrangement) {
        updateLayout { horizontalArrangement = value }
    }

    fun setAppLayoutVerticalArrangement(value: Model.Settings.VerticalArrangement) {
        updateLayout { verticalArrangement = value }
    }

    fun setAppLayoutSortBy(value: Model.Settings.SortBy) {
        updateLayout { sortBy = value }
    }

    fun setAppCardLabelRemoveSpaces(value: Boolean) {
        updateAppCard { labelRemoveSpaces = value }
    }

    fun setAppCardLabelLowercase(value: Boolean) {
        updateAppCard { labelRemoveSpaces = value }
    }

    fun setAppCardFontFamily(value: Model.Settings.FontFamily) {
        updateAppCard { fontFamily = value }
    }

    fun setAppCardTextStyle(value: Model.Settings.TextStyle) {
        updateAppCard { textStyle = value }
    }

    fun setAppCardTextColor(value: Model.Settings.TextColor) {
        updateAppCard { textColor = value }
    }

    fun setAppCardPadding(value: Int) {
        updateAppCard { padding = value }
    }

    fun setAppCardSplitScreenShortcutSeparator(value: String) {
        updateAppCard { splitScreenSeparator = value }
    }

    fun updateSettingsFromStream(getStream: () -> InputStream?) {
        stateAction {
            getStream()?.use { stream ->
                this.updateSettingsFromInputStream(stream)
            }
        }
    }

    fun writeSettingsToFile(getFile: () -> ParcelFileDescriptor?) {
        stateAction {
            getFile()?.use { file ->
                FileOutputStream(file.fileDescriptor).use { stream ->
                    this.writeSettingsToOutputStream(stream)
                }
            }
        }
    }

    private fun updateAppCard(action: Model.Settings.AppCard.Builder.() -> Unit) {
        stateAction {
            updateSettings {
                appCard = appCard.toBuilder().apply(action).build()
            }
        }
    }

    private fun updateLayout(action: Model.Settings.Layout.Builder.() -> Unit) {
        stateAction {
            updateSettings {
                layout = layout.toBuilder().apply(action).build()
            }
        }
    }
}