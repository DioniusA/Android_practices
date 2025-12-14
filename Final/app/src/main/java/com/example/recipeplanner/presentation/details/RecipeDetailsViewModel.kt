package com.example.recipeplanner.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.usecase.favorite.IsFavoriteUseCase
import com.example.recipeplanner.domain.usecase.favorite.ToggleFavoriteUseCase
import com.example.recipeplanner.domain.usecase.mealplan.AddToMealPlanUseCase
import com.example.recipeplanner.domain.usecase.recipe.GetRecipeDetailsUseCase
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
import java.time.LocalDate
import javax.inject.Inject

data class RecipeDetailsUiState(
    val recipe: UiState<Recipe> = UiState.Loading,
    val isFavorite: Boolean = false,
    val showMealPlanDialog: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMealType: MealType = MealType.DINNER
)

sealed class RecipeDetailsEvent {
    data object ToggleFavorite : RecipeDetailsEvent()
    data object StartCookMode : RecipeDetailsEvent()
    data object ShowMealPlanDialog : RecipeDetailsEvent()
    data object DismissMealPlanDialog : RecipeDetailsEvent()
    data class SelectDate(val date: LocalDate) : RecipeDetailsEvent()
    data class SelectMealType(val mealType: MealType) : RecipeDetailsEvent()
    data object AddToMealPlan : RecipeDetailsEvent()
    data object Retry : RecipeDetailsEvent()
}

@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeDetailsUseCase: GetRecipeDetailsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val addToMealPlanUseCase: AddToMealPlanUseCase
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(RecipeDetailsUiState())
    val uiState: StateFlow<RecipeDetailsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    init {
        loadRecipe()
        observeFavoriteStatus()
    }

    fun onEvent(event: RecipeDetailsEvent) {
        when (event) {
            RecipeDetailsEvent.ToggleFavorite -> toggleFavorite()
            RecipeDetailsEvent.StartCookMode -> {
                viewModelScope.launch {
                    _effects.emit(UiEffect.Navigate("cook_mode/$recipeId"))
                }
            }
            RecipeDetailsEvent.ShowMealPlanDialog -> {
                _uiState.update { it.copy(showMealPlanDialog = true) }
            }
            RecipeDetailsEvent.DismissMealPlanDialog -> {
                _uiState.update { it.copy(showMealPlanDialog = false) }
            }
            is RecipeDetailsEvent.SelectDate -> {
                _uiState.update { it.copy(selectedDate = event.date) }
            }
            is RecipeDetailsEvent.SelectMealType -> {
                _uiState.update { it.copy(selectedMealType = event.mealType) }
            }
            RecipeDetailsEvent.AddToMealPlan -> addToMealPlan()
            RecipeDetailsEvent.Retry -> loadRecipe()
        }
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(recipe = UiState.Loading) }
            
            when (val result = getRecipeDetailsUseCase(recipeId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(recipe = UiState.Success(result.data)) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(recipe = UiState.Error(result.error.message)) }
                }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            isFavoriteUseCase(recipeId).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    private fun toggleFavorite() {
        val recipe = (_uiState.value.recipe as? UiState.Success)?.data ?: return
        
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(recipe)) {
                is AppResult.Success -> {
                    val message = if (result.data) "Added to favorites" else "Removed from favorites"
                    _effects.emit(UiEffect.ShowSnackbar(message))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun addToMealPlan() {
        val recipe = (_uiState.value.recipe as? UiState.Success)?.data ?: return
        val state = _uiState.value
        
        viewModelScope.launch {
            when (val result = addToMealPlanUseCase(recipe, state.selectedDate, state.selectedMealType)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(showMealPlanDialog = false) }
                    _effects.emit(UiEffect.ShowSnackbar("Added to meal plan"))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }
}
