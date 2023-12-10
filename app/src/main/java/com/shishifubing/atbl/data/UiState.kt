package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UiState<T> {
    @Immutable
    interface Loading<T> : UiState<T>
    data class Success<T>(val state: T) : UiState<T>
}