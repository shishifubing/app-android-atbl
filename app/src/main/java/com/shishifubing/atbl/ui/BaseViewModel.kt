package com.shishifubing.atbl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.LauncherNavigator
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.data.HomeState
import com.shishifubing.atbl.data.RepoState
import com.shishifubing.atbl.data.UiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val tag = HomeState::class.java.name


abstract class BaseViewModel<T>(
    private val stateRepo: LauncherStateRepository,
    private val navigator: LauncherNavigator
) : ViewModel(), LauncherNavigator by navigator {

    private val _errorFlow = MutableStateFlow<Throwable?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _errorFlow.update { e }
    }

    private var prevState = Defaults.State

    abstract val uiStateFlow: StateFlow<UiState<T>>

    protected val stateFlow = stateRepo.observeState()
        .map { stateResult ->
            RepoState.Success(stateResult.fold(
                onSuccess = {
                    prevState = it
                    it
                },
                onFailure = {
                    _errorFlow.update { it }
                    prevState
                }
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RepoState.Loading
        )

    protected fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    protected fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }
}



