package com.example.recipeplanner.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.IngredientCategory
import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.repository.MealPlanRepository
import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.usecase.shoppinglist.ClearCheckedItemsUseCase
import com.example.recipeplanner.domain.usecase.shoppinglist.GenerateShoppingListUseCase
import com.example.recipeplanner.domain.usecase.shoppinglist.ToggleShoppingItemUseCase
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingListUiState(
    val items: UiState<Map<IngredientCategory, List<ShoppingListItem>>> = UiState.Loading,
    val searchQuery: String = "",
    val isGenerating: Boolean = false
)

sealed class ShoppingListEvent {
    data class ToggleItem(val itemId: String) : ShoppingListEvent()
    data class SearchQueryChanged(val query: String) : ShoppingListEvent()
    data object ClearChecked : ShoppingListEvent()
    data object GenerateFromMealPlan : ShoppingListEvent()
    data object Refresh : ShoppingListEvent()
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val authRepository: AuthRepository,
    private val toggleShoppingItemUseCase: ToggleShoppingItemUseCase,
    private val clearCheckedItemsUseCase: ClearCheckedItemsUseCase,
    private val generateShoppingListUseCase: GenerateShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    private var allItems: List<ShoppingListItem> = emptyList()

    init {
        loadShoppingList()
        syncShoppingList()
    }

    fun onEvent(event: ShoppingListEvent) {
        when (event) {
            is ShoppingListEvent.ToggleItem -> toggleItem(event.itemId)
            is ShoppingListEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterItems()
            }
            ShoppingListEvent.ClearChecked -> clearChecked()
            ShoppingListEvent.GenerateFromMealPlan -> generateFromMealPlan()
            ShoppingListEvent.Refresh -> syncShoppingList()
        }
    }

    private fun loadShoppingList() {
        viewModelScope.launch {
            shoppingListRepository.getShoppingList().collect { items ->
                allItems = items
                filterItems()
            }
        }
    }

    private fun filterItems() {
        val query = _uiState.value.searchQuery.lowercase()
        val filtered = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter { it.ingredientName.lowercase().contains(query) }
        }

        val grouped = filtered.groupBy { it.category }
            .toSortedMap()

        _uiState.update {
            it.copy(
                items = if (filtered.isEmpty()) {
                    if (allItems.isEmpty()) {
                        UiState.Empty("Shopping list is empty.\nGenerate from your meal plan!")
                    } else {
                        UiState.Empty("No items match your search")
                    }
                } else {
                    UiState.Success(grouped)
                }
            )
        }
    }

    private fun syncShoppingList() {
        viewModelScope.launch {
            shoppingListRepository.syncShoppingList()
        }
    }

    private fun toggleItem(itemId: String) {
        viewModelScope.launch {
            toggleShoppingItemUseCase(itemId)
        }
    }

    private fun clearChecked() {
        viewModelScope.launch {
            when (val result = clearCheckedItemsUseCase()) {
                is AppResult.Success -> {
                    _effects.emit(UiEffect.ShowSnackbar("Cleared checked items"))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }

    private fun generateFromMealPlan() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            
            _uiState.update { it.copy(isGenerating = true) }
            
            val mealPlanEntries = mealPlanRepository.getMealPlan().first()
            
            if (mealPlanEntries.isEmpty()) {
                _effects.emit(UiEffect.ShowSnackbar("No meals in your plan. Add some first!"))
                _uiState.update { it.copy(isGenerating = false) }
                return@launch
            }
            
            when (val result = generateShoppingListUseCase(mealPlanEntries, userId)) {
                is AppResult.Success -> {
                    _effects.emit(UiEffect.ShowSnackbar("Shopping list generated with ${result.data.size} items"))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
            
            _uiState.update { it.copy(isGenerating = false) }
        }
    }
}
