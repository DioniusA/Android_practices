package com.example.recipeplanner.presentation.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recipeplanner.domain.model.Category
import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.presentation.common.UiState
import com.example.recipeplanner.presentation.components.CategoryFilterChips
import com.example.recipeplanner.presentation.components.EmptyView
import com.example.recipeplanner.presentation.components.ErrorView
import com.example.recipeplanner.presentation.components.LoadingView
import com.example.recipeplanner.presentation.components.RecipeCard
import com.example.recipeplanner.presentation.components.RecipeSearchBar
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreUiState,
    onEvent: (ExploreEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore Recipes") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            RecipeSearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(ExploreEvent.SearchQueryChanged(it)) },
                onSearch = { onEvent(ExploreEvent.Search) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category chips
            if (state.categories.isNotEmpty()) {
                CategoryFilterChips(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { onEvent(ExploreEvent.CategorySelected(it)) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Recipe list
            when (val recipesState = state.recipes) {
                is UiState.Loading -> {
                    LoadingView(message = "Loading recipes...")
                }
                is UiState.Empty -> {
                    EmptyView(message = recipesState.message)
                }
                is UiState.Error -> {
                    ErrorView(
                        message = recipesState.message,
                        onRetry = { onEvent(ExploreEvent.Retry) }
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = recipesState.data,
                            key = { it.id }
                        ) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                isFavorite = state.favoriteIds.contains(recipe.id),
                                onRecipeClick = { onEvent(ExploreEvent.RecipeClicked(recipe.id)) },
                                onFavoriteClick = { onEvent(ExploreEvent.ToggleFavorite(recipe)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    RecipePlannerTheme {
        ExploreScreen(
            state = ExploreUiState(
                recipes = UiState.Success(
                    listOf(
                        Recipe(
                            id = "1",
                            name = "Chicken Curry",
                            category = "Chicken",
                            area = "Indian",
                            instructions = "",
                            thumbnailUrl = "",
                            youtubeUrl = null,
                            ingredients = listOf(Ingredient("Chicken", "500g")),
                            tags = emptyList(),
                            sourceUrl = null
                        )
                    )
                ),
                categories = listOf(
                    Category("1", "Beef", "", ""),
                    Category("2", "Chicken", "", ""),
                    Category("3", "Dessert", "", "")
                )
            ),
            onEvent = {}
        )
    }
}
