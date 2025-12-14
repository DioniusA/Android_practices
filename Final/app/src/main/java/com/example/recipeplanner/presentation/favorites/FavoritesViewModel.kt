package com.example.recipeplanner.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.FavoriteRecipe
import com.example.recipeplanner.domain.repository.FavoriteRepository
import com.example.recipeplanner.domain.util.AppResult
import com.example.recipeplanner.presentation.common.UiEffect
import com.example.recipeplanner.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: UiState<List<FavoriteRecipe>> = UiState.Loading
)

sealed class FavoritesEvent {
    data class RecipeClicked(val recipeId: String) : FavoritesEvent()
    data class RemoveFromFavorites(val recipeId: String) : FavoritesEvent()
    data object Refresh : FavoritesEvent()
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    init {
        loadFavorites()
        syncFavorites()
    }

    fun onEvent(event: FavoritesEvent) {
        when (event) {
            is FavoritesEvent.RecipeClicked -> {
                viewModelScope.launch {
                    _effects.emit(UiEffect.Navigate("recipe/${event.recipeId}"))
                }
            }
            is FavoritesEvent.RemoveFromFavorites -> removeFromFavorites(event.recipeId)
            FavoritesEvent.Refresh -> syncFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect { favorites ->
                _uiState.update {
                    it.copy(
                        favorites = if (favorites.isEmpty()) {
                            UiState.Empty("No favorite recipes yet.\nStart exploring and add some!")
                        } else {
                            UiState.Success(favorites)
                        }
                    )
                }
            }
        }
    }

    private fun syncFavorites() {
        viewModelScope.launch {
            favoriteRepository.syncFavorites()
        }
    }

    private fun removeFromFavorites(recipeId: String) {
        viewModelScope.launch {
            when (val result = favoriteRepository.removeFromFavorites(recipeId)) {
                is AppResult.Success -> {
                    _effects.emit(UiEffect.ShowSnackbar("Removed from favorites"))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }
}
