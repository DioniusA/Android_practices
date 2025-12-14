package com.example.recipeplanner.domain.repository

import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for shopping list operations.
 * Syncs between Supabase (remote) and Room (local cache).
 */
interface ShoppingListRepository {
    /**
     * Flow of all shopping list items for the current user.
     */
    fun getShoppingList(): Flow<List<ShoppingListItem>>

    /**
     * Adds an item to the shopping list.
     */
    suspend fun addItem(item: ShoppingListItem): AppResult<Unit>

    /**
     * Adds multiple items to the shopping list.
     */
    suspend fun addItems(items: List<ShoppingListItem>): AppResult<Unit>

    /**
     * Toggles the checked state of an item.
     */
    suspend fun toggleItemChecked(itemId: String): AppResult<Unit>

    /**
     * Removes an item from the shopping list.
     */
    suspend fun removeItem(itemId: String): AppResult<Unit>

    /**
     * Clears all checked items from the shopping list.
     */
    suspend fun clearCheckedItems(): AppResult<Unit>

    /**
     * Clears all items from the shopping list.
     */
    suspend fun clearAll(): AppResult<Unit>

    /**
     * Syncs shopping list from remote to local cache.
     */
    suspend fun syncShoppingList(): AppResult<Unit>
}
