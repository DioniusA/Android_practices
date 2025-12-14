package com.example.recipeplanner.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipeplanner.domain.model.FavoriteRecipe
import com.example.recipeplanner.presentation.common.UiState
import com.example.recipeplanner.presentation.components.CategoryChip
import com.example.recipeplanner.presentation.components.EmptyView
import com.example.recipeplanner.presentation.components.LoadingView
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onEvent: (FavoritesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val favoritesState = state.favorites) {
            is UiState.Loading -> {
                LoadingView(
                    message = "Loading favorites...",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Empty -> {
                EmptyView(
                    message = favoritesState.message,
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Error -> {
                EmptyView(
                    message = favoritesState.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = favoritesState.data,
                        key = { it.id }
                    ) { favorite ->
                        FavoriteRecipeItem(
                            favorite = favorite,
                            onClick = { onEvent(FavoritesEvent.RecipeClicked(favorite.recipeId)) },
                            onRemove = { onEvent(FavoritesEvent.RemoveFromFavorites(favorite.recipeId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRecipeItem(
    favorite: FavoriteRecipe,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = favorite.recipeImageUrl,
                contentDescription = favorite.recipeName,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = favorite.recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (favorite.recipeCategory.isNotBlank()) {
                    CategoryChip(
                        category = favorite.recipeCategory,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    RecipePlannerTheme {
        FavoritesScreen(
            state = FavoritesUiState(
                favorites = UiState.Success(
                    listOf(
                        FavoriteRecipe(
                            id = "1",
                            recipeId = "r1",
                            userId = "u1",
                            recipeName = "Chicken Curry",
                            recipeImageUrl = "",
                            recipeCategory = "Chicken",
                            addedAt = Instant.now()
                        ),
                        FavoriteRecipe(
                            id = "2",
                            recipeId = "r2",
                            userId = "u1",
                            recipeName = "Pasta Carbonara",
                            recipeImageUrl = "",
                            recipeCategory = "Pasta",
                            addedAt = Instant.now()
                        )
                    )
                )
            ),
            onEvent = {}
        )
    }
}
