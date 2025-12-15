package com.example.recipeplanner.presentation.common

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String = "No data available") : UiState<Nothing>()
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isEmpty: Boolean get() = this is Empty

    fun getDataOrNull(): T? = (this as? Success)?.data
}

sealed class UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect()
    data class Navigate(val route: String) : UiEffect()
    data object NavigateBack : UiEffect()
}
