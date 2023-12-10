package com.shishifubing.atbl.ui


import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.HomeState
import com.shishifubing.atbl.launcherViewModelFactory

private val tag = HomeState::class.java.name


class EditSplitScreenViewModel(
    private val stateRepo: LauncherStateRepository
) : StateViewModel(stateRepo) {
    companion object {
        val Factory = launcherViewModelFactory {
            EditSplitScreenViewModel(stateRepo = stateRepo)
        }
    }

    fun removeSplitScreenShortcut(shortcut: Model.SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut) }
    }

    fun addSplitScreenShortcut(firstApp: Model.App, secondApp: Model.App) {
        stateAction {
            addSplitScreenShortcut(firstApp.packageName, secondApp.packageName)
        }
    }

}



