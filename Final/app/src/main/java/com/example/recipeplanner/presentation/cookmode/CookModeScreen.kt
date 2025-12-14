package com.example.recipeplanner.presentation.cookmode

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.presentation.common.UiState
import com.example.recipeplanner.presentation.components.ErrorView
import com.example.recipeplanner.presentation.components.LoadingView
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookModeScreen(
    state: CookModeUiState,
    onEvent: (CookModeEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Cook Mode",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(CookModeEvent.ToggleIngredients) }) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Show ingredients")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val recipeState = state.recipe) {
            is UiState.Loading -> {
                LoadingView(modifier = Modifier.padding(paddingValues))
            }
            is UiState.Error -> {
                ErrorView(
                    message = recipeState.message,
                    onRetry = { onEvent(CookModeEvent.Retry) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Empty -> {
                ErrorView(
                    message = "Recipe not found",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Success -> {
                CookModeContent(
                    recipe = recipeState.data,
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
        
        // Ingredients bottom sheet
        if (state.showIngredients) {
            val recipe = (state.recipe as? UiState.Success)?.data
            if (recipe != null) {
                ModalBottomSheet(
                    onDismissRequest = { onEvent(CookModeEvent.ToggleIngredients) },
                    sheetState = sheetState
                ) {
                    IngredientsSheet(ingredients = recipe.ingredients)
                }
            }
        }
    }
}

@Composable
private fun CookModeContent(
    recipe: Recipe,
    state: CookModeUiState,
    onEvent: (CookModeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator + step count in one row
        val progress = if (state.steps.isNotEmpty()) {
            (state.currentStepIndex + 1).toFloat() / state.steps.size
        } else 0f
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${state.currentStepIndex + 1}/${state.steps.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Step content with animation - takes maximum available space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = state.currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "step_animation"
            ) { stepIndex ->
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = state.steps.getOrElse(stepIndex) { "" },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                lineHeight = 28.sp
                            ),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Navigation buttons - compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onEvent(CookModeEvent.PreviousStep) },
                enabled = state.currentStepIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Prev")
            }
            
            Button(
                onClick = { onEvent(CookModeEvent.NextStep) },
                enabled = state.currentStepIndex < state.steps.size - 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Timer section - compact
        TimerSection(
            timerSeconds = state.timerSeconds,
            isRunning = state.isTimerRunning,
            onSetTimer = { onEvent(CookModeEvent.SetTimer(it)) },
            onStart = { onEvent(CookModeEvent.StartTimer) },
            onPause = { onEvent(CookModeEvent.PauseTimer) },
            onReset = { onEvent(CookModeEvent.ResetTimer) }
        )
    }
}

@Composable
private fun TimerSection(
    timerSeconds: Int,
    isRunning: Boolean,
    onSetTimer: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact timer: [−] 05:30 [+] [▶Start] [Reset]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Minus button (removes 1 minute)
                IconButton(
                    onClick = { if (timerSeconds >= 60) onSetTimer(timerSeconds - 60) },
                    enabled = !isRunning && timerSeconds >= 60,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Remove minute")
                }
                
                // Timer display
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Plus button (adds 1 minute)
                IconButton(
                    onClick = { onSetTimer(timerSeconds + 60) },
                    enabled = !isRunning && minutes < 99,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add minute")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Control buttons
                if (timerSeconds > 0) {
                    FilledTonalButton(
                        onClick = if (isRunning) onPause else onStart,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRunning) "Pause" else "Start")
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            }
            
            // Quick preset buttons (only when not running)
            if (!isRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1, 2, 3, 5, 10, 15, 20, 30).forEach { mins ->
                        OutlinedButton(
                            onClick = { onSetTimer(mins * 60) },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "$mins",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientsSheet(
    ingredients: List<Ingredient>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = ingredient.measure,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun CookModeScreenPreview() {
    RecipePlannerTheme {
        CookModeScreen(
            state = CookModeUiState(
                recipe = UiState.Success(
                    Recipe(
                        id = "1",
                        name = "Chicken Curry",
                        category = "Chicken",
                        area = "Indian",
                        instructions = "",
                        thumbnailUrl = "",
                        youtubeUrl = null,
                        ingredients = listOf(
                            Ingredient("Chicken", "500g"),
                            Ingredient("Onion", "2 medium")
                        ),
                        tags = emptyList(),
                        sourceUrl = null
                    )
                ),
                steps = listOf(
                    "Heat oil in a large pan over medium heat. Add the onions and cook until golden brown, about 8-10 minutes.",
                    "Add the garlic and ginger, cook for another minute until fragrant.",
                    "Add the chicken pieces and brown on all sides.",
                    "Add the curry powder, turmeric, and other spices. Stir well to coat the chicken.",
                    "Pour in the coconut milk and bring to a simmer. Cook for 20-25 minutes until chicken is cooked through."
                ),
                currentStepIndex = 0,
                timerSeconds = 300
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}
