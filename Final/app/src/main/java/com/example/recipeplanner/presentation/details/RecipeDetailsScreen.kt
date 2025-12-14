package com.example.recipeplanner.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.presentation.common.UiState
import com.example.recipeplanner.presentation.components.CategoryChip
import com.example.recipeplanner.presentation.components.ErrorView
import com.example.recipeplanner.presentation.components.LoadingView
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    state: RecipeDetailsUiState,
    onEvent: (RecipeDetailsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(RecipeDetailsEvent.ToggleFavorite) }) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
                    onRetry = { onEvent(RecipeDetailsEvent.Retry) },
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
                RecipeDetailsContent(
                    recipe = recipeState.data,
                    onStartCookMode = { onEvent(RecipeDetailsEvent.StartCookMode) },
                    onAddToMealPlan = { onEvent(RecipeDetailsEvent.ShowMealPlanDialog) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
        
        // Meal plan dialog
        if (state.showMealPlanDialog) {
            MealPlanDialog(
                selectedDate = state.selectedDate,
                selectedMealType = state.selectedMealType,
                onDateSelected = { onEvent(RecipeDetailsEvent.SelectDate(it)) },
                onMealTypeSelected = { onEvent(RecipeDetailsEvent.SelectMealType(it)) },
                onConfirm = { onEvent(RecipeDetailsEvent.AddToMealPlan) },
                onDismiss = { onEvent(RecipeDetailsEvent.DismissMealPlanDialog) }
            )
        }
    }
}

@Composable
private fun RecipeDetailsContent(
    recipe: Recipe,
    onStartCookMode: () -> Unit,
    onAddToMealPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Recipe image
        AsyncImage(
            model = recipe.thumbnailUrl,
            contentDescription = recipe.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            contentScale = ContentScale.Crop
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and category
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(category = recipe.category)
                if (recipe.area.isNotBlank()) {
                    Text(
                        text = recipe.area,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartCookMode,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cook Mode")
                }
                
                FilledTonalButton(
                    onClick = onAddToMealPlan,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Plan")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Ingredients
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    recipe.ingredients.forEach { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = ingredient.measure,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Instructions
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recipe.instructions,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanDialog(
    selectedDate: LocalDate,
    selectedMealType: MealType,
    onDateSelected: (LocalDate) -> Unit,
    onMealTypeSelected: (MealType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Meal Plan") },
        text = {
            Column {
                Text(
                    text = "Select date and meal type",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date selection (simple: today + next 6 days)
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val today = LocalDate.now()
                    (0..6).forEach { dayOffset ->
                        val date = today.plusDays(dayOffset.toLong())
                        val isSelected = date == selectedDate
                        
                        OutlinedButton(
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f),
                            colors = if (isSelected) {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("EEE\ndd")),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal type selection
                Text(
                    text = "Meal Type",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    MealType.entries.forEachIndexed { index, mealType ->
                        SegmentedButton(
                            selected = mealType == selectedMealType,
                            onClick = { onMealTypeSelected(mealType) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = MealType.entries.size
                            )
                        ) {
                            Text(mealType.displayName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun RecipeDetailsScreenPreview() {
    RecipePlannerTheme {
        RecipeDetailsScreen(
            state = RecipeDetailsUiState(
                recipe = UiState.Success(
                    Recipe(
                        id = "1",
                        name = "Chicken Curry",
                        category = "Chicken",
                        area = "Indian",
                        instructions = "1. Prepare the ingredients...\n\n2. Cook the chicken...\n\n3. Add spices...",
                        thumbnailUrl = "",
                        youtubeUrl = null,
                        ingredients = listOf(
                            Ingredient("Chicken", "500g"),
                            Ingredient("Onion", "2 medium"),
                            Ingredient("Garlic", "4 cloves"),
                            Ingredient("Curry powder", "2 tbsp")
                        ),
                        tags = listOf("Spicy", "Main"),
                        sourceUrl = null
                    )
                ),
                isFavorite = true
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}
