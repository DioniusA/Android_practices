package com.example.recipeplanner.presentation.shoppinglist

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recipeplanner.domain.model.IngredientCategory
import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.presentation.common.UiState
import com.example.recipeplanner.presentation.components.EmptyView
import com.example.recipeplanner.presentation.components.LoadingView
import com.example.recipeplanner.presentation.components.RecipeSearchBar
import com.example.recipeplanner.presentation.theme.RecipePlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    state: ShoppingListUiState,
    onEvent: (ShoppingListEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                actions = {
                    IconButton(onClick = { onEvent(ShoppingListEvent.ClearChecked) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear checked items")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(ShoppingListEvent.GenerateFromMealPlan) },
                icon = {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                },
                text = { Text("Generate") }
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
                onQueryChange = { onEvent(ShoppingListEvent.SearchQueryChanged(it)) },
                onSearch = {},
                placeholder = "Search ingredients..."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (val itemsState = state.items) {
                is UiState.Loading -> {
                    LoadingView(message = "Loading shopping list...")
                }
                is UiState.Empty -> {
                    EmptyView(
                        message = itemsState.message,
                        icon = Icons.Default.ShoppingCart
                    )
                }
                is UiState.Error -> {
                    EmptyView(message = itemsState.message)
                }
                is UiState.Success -> {
                    ShoppingListContent(
                        groupedItems = itemsState.data,
                        onToggleItem = { onEvent(ShoppingListEvent.ToggleItem(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListContent(
    groupedItems: Map<IngredientCategory, List<ShoppingListItem>>,
    onToggleItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedItems.forEach { (category, items) ->
            item(key = category.name) {
                CategorySection(
                    category = category,
                    items = items,
                    onToggleItem = onToggleItem
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: IngredientCategory,
    items: List<ShoppingListItem>,
    onToggleItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            items.forEach { item ->
                ShoppingListItemRow(
                    item = item,
                    onToggle = { onToggleItem(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ShoppingListItemRow(
    item: ShoppingListItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (item.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (item.isChecked) "Checked" else "Unchecked",
            tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.ingredientName,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (item.quantity.isNotBlank()) {
                Text(
                    text = item.quantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenPreview() {
    RecipePlannerTheme {
        ShoppingListScreen(
            state = ShoppingListUiState(
                items = UiState.Success(
                    mapOf(
                        IngredientCategory.PRODUCE to listOf(
                            ShoppingListItem(
                                id = "1",
                                userId = "u1",
                                ingredientName = "Onion",
                                quantity = "2 medium",
                                category = IngredientCategory.PRODUCE,
                                isChecked = false,
                                recipeIds = listOf("r1")
                            ),
                            ShoppingListItem(
                                id = "2",
                                userId = "u1",
                                ingredientName = "Garlic",
                                quantity = "4 cloves",
                                category = IngredientCategory.PRODUCE,
                                isChecked = true,
                                recipeIds = listOf("r1", "r2")
                            )
                        ),
                        IngredientCategory.MEAT to listOf(
                            ShoppingListItem(
                                id = "3",
                                userId = "u1",
                                ingredientName = "Chicken breast",
                                quantity = "500g",
                                category = IngredientCategory.MEAT,
                                isChecked = false,
                                recipeIds = listOf("r1")
                            )
                        )
                    )
                )
            ),
            onEvent = {}
        )
    }
}
