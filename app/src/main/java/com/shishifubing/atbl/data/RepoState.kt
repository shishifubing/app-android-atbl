package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable
import com.shishifubing.atbl.Model

@Immutable
sealed interface RepoState {
    data object Loading : RepoState
    data class Success(val state: Model.State) : RepoState
}