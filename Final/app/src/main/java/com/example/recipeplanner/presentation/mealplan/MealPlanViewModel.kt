package com.example.recipeplanner.presentation.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.repository.MealPlanRepository
import com.example.recipeplanner.domain.usecase.mealplan.RemoveFromMealPlanUseCase
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class MealPlanUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val mealPlan: UiState<Map<LocalDate, Map<MealType, List<MealPlanEntry>>>> = UiState.Loading
)

sealed class MealPlanEvent {
    data object PreviousWeek : MealPlanEvent()
    data object NextWeek : MealPlanEvent()
    data class RecipeClicked(val recipeId: String) : MealPlanEvent()
    data class RemoveEntry(val entryId: String) : MealPlanEvent()
    data object GenerateShoppingList : MealPlanEvent()
    data object Refresh : MealPlanEvent()
}

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val removeFromMealPlanUseCase: RemoveFromMealPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    init {
        loadMealPlan()
        syncMealPlan()
    }

    fun onEvent(event: MealPlanEvent) {
        when (event) {
            MealPlanEvent.PreviousWeek -> {
                _uiState.update { it.copy(weekStart = it.weekStart.minusWeeks(1)) }
            }
            MealPlanEvent.NextWeek -> {
                _uiState.update { it.copy(weekStart = it.weekStart.plusWeeks(1)) }
            }
            is MealPlanEvent.RecipeClicked -> {
                viewModelScope.launch {
                    _effects.emit(UiEffect.Navigate("recipe/${event.recipeId}"))
                }
            }
            is MealPlanEvent.RemoveEntry -> removeEntry(event.entryId)
            MealPlanEvent.GenerateShoppingList -> {
                viewModelScope.launch {
                    _effects.emit(UiEffect.Navigate("shopping_list"))
                }
            }
            MealPlanEvent.Refresh -> syncMealPlan()
        }
    }

    private fun loadMealPlan() {
        viewModelScope.launch {
            mealPlanRepository.getMealPlan().collect { entries ->
                val grouped = entries.groupBy { it.date }
                    .mapValues { (_, dayEntries) ->
                        dayEntries.groupBy { it.mealType }
                    }
                
                _uiState.update {
                    it.copy(
                        mealPlan = if (entries.isEmpty()) {
                            UiState.Empty("No meals planned yet.\nAdd recipes from the Explore tab!")
                        } else {
                            UiState.Success(grouped)
                        }
                    )
                }
            }
        }
    }

    private fun syncMealPlan() {
        viewModelScope.launch {
            mealPlanRepository.syncMealPlan()
        }
    }

    private fun removeEntry(entryId: String) {
        viewModelScope.launch {
            when (val result = removeFromMealPlanUseCase(entryId)) {
                is AppResult.Success -> {
                    _effects.emit(UiEffect.ShowSnackbar("Removed from meal plan"))
                }
                is AppResult.Error -> {
                    _effects.emit(UiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }
}
