package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

@Immutable
sealed interface SettingsScreenUIState {
    @Immutable
    data class Success(val state: Model.State) : SettingsScreenUIState

    @Immutable
    data object Loading : SettingsScreenUIState
}