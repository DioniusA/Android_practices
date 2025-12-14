package com.example.recipeplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.recipeplanner.data.local.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for shopping list operations.
 */
@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list WHERE userId = :userId ORDER BY category, ingredientName")
    fun getShoppingListByUser(userId: String): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_list ORDER BY category, ingredientName")
    fun getAllShoppingList(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_list WHERE id = :id")
    suspend fun getById(id: String): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingListEntity>)

    @Query("UPDATE shopping_list SET isChecked = NOT isChecked WHERE id = :id")
    suspend fun toggleChecked(id: String)

    @Query("DELETE FROM shopping_list WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM shopping_list WHERE isChecked = 1 AND userId = :userId")
    suspend fun deleteCheckedByUser(userId: String)

    @Query("DELETE FROM shopping_list WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM shopping_list")
    suspend fun clearAll()
}
