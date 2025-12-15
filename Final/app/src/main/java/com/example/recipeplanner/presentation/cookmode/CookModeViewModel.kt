package com.example.recipeplanner.presentation.cookmode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.usecase.recipe.GetRecipeDetailsUseCase
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

data class CookModeUiState(
    val recipe: UiState<Recipe> = UiState.Loading,
    val currentStepIndex: Int = 0,
    val steps: List<String> = emptyList(),
    val timerSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val showIngredients: Boolean = false
)

sealed class CookModeEvent {
    data object NextStep : CookModeEvent()
    data object PreviousStep : CookModeEvent()
    data object ToggleIngredients : CookModeEvent()
    data class SetTimer(val seconds: Int) : CookModeEvent()
    data object StartTimer : CookModeEvent()
    data object PauseTimer : CookModeEvent()
    data object ResetTimer : CookModeEvent()
    data object Retry : CookModeEvent()
}

@HiltViewModel
class CookModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeDetailsUseCase: GetRecipeDetailsUseCase
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow(CookModeUiState())
    val uiState: StateFlow<CookModeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null

    init {
        loadRecipe()
    }

    fun onEvent(event: CookModeEvent) {
        when (event) {
            CookModeEvent.NextStep -> {
                val steps = _uiState.value.steps
                val currentIndex = _uiState.value.currentStepIndex
                if (currentIndex < steps.size - 1) {
                    _uiState.update { it.copy(currentStepIndex = currentIndex + 1) }
                }
            }
            CookModeEvent.PreviousStep -> {
                val currentIndex = _uiState.value.currentStepIndex
                if (currentIndex > 0) {
                    _uiState.update { it.copy(currentStepIndex = currentIndex - 1) }
                }
            }
            CookModeEvent.ToggleIngredients -> {
                _uiState.update { it.copy(showIngredients = !it.showIngredients) }
            }
            is CookModeEvent.SetTimer -> {
                _uiState.update { it.copy(timerSeconds = event.seconds, isTimerRunning = false) }
                timerJob?.cancel()
            }
            CookModeEvent.StartTimer -> startTimer()
            CookModeEvent.PauseTimer -> pauseTimer()
            CookModeEvent.ResetTimer -> {
                timerJob?.cancel()
                _uiState.update { it.copy(timerSeconds = 0, isTimerRunning = false) }
            }
            CookModeEvent.Retry -> loadRecipe()
        }
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(recipe = UiState.Loading) }
            
            when (val result = getRecipeDetailsUseCase(recipeId)) {
                is AppResult.Success -> {
                    val recipe = result.data
                    val steps = parseInstructions(recipe.instructions)
                    _uiState.update { 
                        it.copy(
                            recipe = UiState.Success(recipe),
                            steps = steps,
                            currentStepIndex = 0
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(recipe = UiState.Error(result.error.message)) }
                }
            }
        }
    }

    private fun parseInstructions(instructions: String): List<String> {
        val steps = instructions
            .split(Regex("(?=\\d+[.)])|(?=Step\\s*\\d+)|\\r?\\n\\r?\\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 10 }
        
        return if (steps.isNotEmpty()) steps else listOf(instructions)
    }

    private fun startTimer() {
        if (_uiState.value.timerSeconds <= 0) return
        
        _uiState.update { it.copy(isTimerRunning = true) }
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
            
            if (_uiState.value.timerSeconds <= 0) {
                _uiState.update { it.copy(isTimerRunning = false) }
                _effects.emit(UiEffect.ShowSnackbar("Timer finished!"))
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
        timerJob?.cancel()
    }
}
