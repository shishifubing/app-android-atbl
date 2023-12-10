package com.shishifubing.atbl.ui

import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.RepoState
import com.shishifubing.atbl.data.UiState
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

open class StateViewModel(
    private val stateRepo: LauncherStateRepository
) : BaseViewModel<Model.State>(stateRepo) {

    companion object {
        val Factory = launcherViewModelFactory {
            StateViewModel(stateRepo = stateRepo)
        }
    }

    override val uiStateFlow = stateFlow.map {
        when (it) {
            RepoState.Loading -> object : UiState.Loading<Model.State> {}
            is RepoState.Success -> UiState.Success(it.state)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = object : UiState.Loading<Model.State> {}
    )

}