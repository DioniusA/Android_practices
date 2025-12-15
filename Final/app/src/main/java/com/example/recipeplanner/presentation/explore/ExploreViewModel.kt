package com.example.recipeplanner.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.usecase.favorite.ToggleFavoriteUseCase
import com.example.recipeplanner.domain.usecase.recipe.GetCategoriesUseCase
import com.example.recipeplanner.domain.usecase.recipe.GetRecipesByCategoryUseCase
import com.example.recipeplanner.domain.usecase.recipe.SearchRecipesUseCase
import com.example.recipeplanner.domain.util.AppResult
import com.example.recipeplanner.presentation.common.UiEffect
import com.example.recipeplanner.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val recipes: UiState<List<Recipe>> = UiState.Loading,
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val favoriteIds: Set<String> = emptySet()
)

sealed class ExploreEvent {
    data class SearchQueryChanged(val query: String) : ExploreEvent()
    data class CategorySelected(val category: String?) : ExploreEvent()
    data class ToggleFavorite(val recipe: Recipe) : ExploreEvent()
    data class RecipeClicked(val recipeId: String) : ExploreEvent()
    data object Search : ExploreEvent()
    data object Retry : ExploreEvent()
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getRecipesByCategoryUseCase: GetRecipesByCategoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadCategories()
        loadInitialRecipes()
    }

    fun onEvent(event: ExploreEvent) {
        when (event) {
            is ExploreEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                debounceSearch()
            }
            is ExploreEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category, searchQuery = "") }
                loadRecipesByCategory(event.category)
            }
            is ExploreEvent.ToggleFavorite -> toggleFavorite(event.recipe)
            is ExploreEvent.RecipeClicked -> {
                viewModelScope.launch {
                    _effects.emit(UiEffect.Navigate("recipe/${event.recipeId}"))
                }
            }
            ExploreEvent.Search -> search()
            ExploreEvent.Retry -> retry()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(categories = result.data) }
                }
                is AppResult.Error -> {
                }
            }
        }
    }

    private fun loadInitialRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(recipes = UiState.Loading) }
            
            when (val result = searchRecipesUseCase("chicken")) {
                is AppResult.Success -> {
                    val recipes = result.data
                    _uiState.update { 
                        it.copy(
                            recipes = if (recipes.isEmpty()) {
                                UiState.Empty("No recipes found. Try searching!")
                            } else {
                                UiState.Success(recipes)
                            }
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { 
                        it.copy(recipes = UiState.Error(result.error.message))
                    }
                }
            }
        }
    }

    private fun debounceSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce for 500ms
            search()
        }
    }

    private fun search() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            loadInitialRecipes()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(recipes = UiState.Loading, selectedCategory = null) }
            
            when (val result = searchRecipesUseCase(query)) {
                is AppResult.Success -> {
                    val recipes = result.data
                    _uiState.update { 
                        it.copy(
                            recipes = if (recipes.isEmpty()) {
                                UiState.Empty("No recipes found for \"$query\"")
                            } else {
                                UiState.Success(recipes)
                            }
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { 
                        it.copy(recipes = UiState.Error(result.error.message))
                    }
                }
            }
        }
    }

    private fun loadRecipesByCategory(category: String?) {
        if (category == null) {
            loadInitialRecipes()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(recipes = UiState.Loading) }
            
            when (val result = getRecipesByCategoryUseCase(category)) {
                is AppResult.Success -> {
                    val recipes = result.data
                    _uiState.update { 
                        it.copy(
                            recipes = if (recipes.isEmpty()) {
                                UiState.Empty("No recipes found in $category")
                            } else {
                                UiState.Success(recipes)
                            }
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { 
                        it.copy(recipes = UiState.Error(result.error.message))
                    }
                }
            }
        }
    }

    private fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(recipe)) {
                is AppResult.Success -> {
                    val isFavorite = result.data
                    _uiState.update { state ->
                        val newFavorites = if (isFavorite) {
                            state.favoriteIds + recipe.id
                        } else {
                            state.favoriteIds - recipe.id
                        }
                        state.copy(favoriteIds = newFavorites)
                    }
                    val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
                    _effects.emit(UiEffect.ShowSnackbar(message))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun retry() {
        val state = _uiState.value
        when {
            state.searchQuery.isNotBlank() -> search()
            state.selectedCategory != null -> loadRecipesByCategory(state.selectedCategory)
            else -> loadInitialRecipes()
        }
    }
}
