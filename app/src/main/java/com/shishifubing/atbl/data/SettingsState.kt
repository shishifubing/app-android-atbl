package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

@Immutable
sealed interface SettingsScreenUIState {
    data class Success(val state: Model.State) : SettingsScreenUIState

    data object Loading : SettingsScreenUIState
}