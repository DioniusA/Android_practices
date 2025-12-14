package com.example.recipeplanner.presentation.mealplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.presentation.common.UiState
import com.example.recipeplanner.presentation.components.EmptyView
import com.example.recipeplanner.presentation.components.LoadingView
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    state: MealPlanUiState,
    onEvent: (MealPlanEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(MealPlanEvent.GenerateShoppingList) }
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Generate shopping list")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Week navigation
            WeekNavigator(
                weekStart = state.weekStart,
                onPreviousWeek = { onEvent(MealPlanEvent.PreviousWeek) },
                onNextWeek = { onEvent(MealPlanEvent.NextWeek) }
            )
            
            when (val planState = state.mealPlan) {
                is UiState.Loading -> {
                    LoadingView(message = "Loading meal plan...")
                }
                is UiState.Empty -> {
                    EmptyView(
                        message = planState.message,
                        icon = Icons.Default.CalendarMonth
                    )
                }
                is UiState.Error -> {
                    EmptyView(message = planState.message)
                }
                is UiState.Success -> {
                    MealPlanWeekView(
                        weekStart = state.weekStart,
                        mealPlan = planState.data,
                        onRecipeClick = { onEvent(MealPlanEvent.RecipeClicked(it)) },
                        onRemove = { onEvent(MealPlanEvent.RemoveEntry(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekNavigator(
    weekStart: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekEnd = weekStart.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous week")
        }
        
        Text(
            text = "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextWeek) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next week")
        }
    }
}

@Composable
private fun MealPlanWeekView(
    weekStart: LocalDate,
    mealPlan: Map<LocalDate, Map<MealType, List<MealPlanEntry>>>,
    onRecipeClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val days = (0..6).map { weekStart.plusDays(it.toLong()) }
        
        items(days) { date ->
            DayMealPlan(
                date = date,
                meals = mealPlan[date] ?: emptyMap(),
                onRecipeClick = onRecipeClick,
                onRemove = onRemove
            )
        }
    }
}

@Composable
private fun DayMealPlan(
    date: LocalDate,
    meals: Map<MealType, List<MealPlanEntry>>,
    onRecipeClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
    val isToday = date == LocalDate.now()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = date.format(dayFormatter) + if (isToday) " (Today)" else "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (meals.isEmpty()) {
                Text(
                    text = "No meals planned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                MealType.entries.forEach { mealType ->
                    val entries = meals[mealType] ?: return@forEach
                    if (entries.isNotEmpty()) {
                        MealTypeSection(
                            mealType = mealType,
                            entries = entries,
                            onRecipeClick = onRecipeClick,
                            onRemove = onRemove
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTypeSection(
    mealType: MealType,
    entries: List<MealPlanEntry>,
    onRecipeClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = mealType.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        entries.forEach { entry ->
            MealPlanEntryItem(
                entry = entry,
                onClick = { onRecipeClick(entry.recipeId) },
                onRemove = { onRemove(entry.id) }
            )
        }
    }
}

@Composable
private fun MealPlanEntryItem(
    entry: MealPlanEntry,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = entry.recipeImageUrl,
            contentDescription = entry.recipeName,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = entry.recipeName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealPlanScreenPreview() {
    RecipePlannerTheme {
        MealPlanScreen(
            state = MealPlanUiState(
                mealPlan = UiState.Success(
                    mapOf(
                        LocalDate.now() to mapOf(
                            MealType.BREAKFAST to listOf(
                                MealPlanEntry(
                                    id = "1",
                                    userId = "u1",
                                    recipeId = "r1",
                                    recipeName = "Pancakes",
                                    recipeImageUrl = "",
                                    date = LocalDate.now(),
                                    mealType = MealType.BREAKFAST
                                )
                            ),
                            MealType.DINNER to listOf(
                                MealPlanEntry(
                                    id = "2",
                                    userId = "u1",
                                    recipeId = "r2",
                                    recipeName = "Chicken Curry",
                                    recipeImageUrl = "",
                                    date = LocalDate.now(),
                                    mealType = MealType.DINNER
                                )
                            )
                        )
                    )
                )
            ),
            onEvent = {}
        )
    }
}
