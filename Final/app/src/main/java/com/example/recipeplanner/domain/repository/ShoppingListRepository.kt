package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getShoppingList(): Flow<List<ShoppingListItem>>
    suspend fun addItem(item: ShoppingListItem): AppResult<Unit>
    suspend fun addItems(items: List<ShoppingListItem>): AppResult<Unit>
    suspend fun toggleItemChecked(itemId: String): AppResult<Unit>
    suspend fun removeItem(itemId: String): AppResult<Unit>
    suspend fun clearCheckedItems(): AppResult<Unit>
    suspend fun clearAll(): AppResult<Unit>
    suspend fun syncShoppingList(): AppResult<Unit>
}
