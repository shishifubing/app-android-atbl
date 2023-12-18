package com.shishifubing.atbl.data

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UiState<T> {
    data class Loading<T>(val placeholder: Unit = Unit) : UiState<T>
    data class Success<T>(val state: T) : UiState<T>
}