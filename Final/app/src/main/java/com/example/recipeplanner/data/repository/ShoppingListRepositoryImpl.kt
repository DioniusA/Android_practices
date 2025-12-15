package com.example.recipeplanner.data.repository

import com.example.recipeplanner.data.local.dao.ShoppingListDao
import com.example.recipeplanner.data.mapper.toDto
import com.example.recipeplanner.data.mapper.toEntity
import com.example.recipeplanner.data.mapper.toShoppingListItem
import com.example.recipeplanner.data.remote.dto.ShoppingListItemDto
import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val shoppingListDao: ShoppingListDao,
    private val authRepository: AuthRepository
) : ShoppingListRepository {

    private val table = "shopping_list_items"

    override fun getShoppingList(): Flow<List<ShoppingListItem>> {
        return shoppingListDao.getAllShoppingList().map { entities ->
            entities.map { it.toShoppingListItem() }
        }
    }

    override suspend fun addItem(item: ShoppingListItem): AppResult<Unit> {
        shoppingListDao.insert(item.toEntity())

        return try {
            supabaseClient.postgrest[table].insert(item.toDto())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync shopping list item to Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun addItems(items: List<ShoppingListItem>): AppResult<Unit> {
        if (items.isEmpty()) return AppResult.Success(Unit)

        shoppingListDao.insertAll(items.map { it.toEntity() })

        return try {
            supabaseClient.postgrest[table].insert(items.map { it.toDto() })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync shopping list items to Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun toggleItemChecked(itemId: String): AppResult<Unit> {
        val item = shoppingListDao.getById(itemId)
            ?: return AppResult.Error(AppError.NotFound("Item not found"))

        shoppingListDao.toggleChecked(itemId)

        return try {
            supabaseClient.postgrest[table].update({
                set("is_checked", !item.isChecked)
            }) {
                filter {
                    eq("id", itemId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle shopping list item in Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun removeItem(itemId: String): AppResult<Unit> {
        shoppingListDao.delete(itemId)

        return try {
            supabaseClient.postgrest[table].delete {
                filter {
                    eq("id", itemId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove shopping list item from Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun clearCheckedItems(): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        shoppingListDao.deleteCheckedByUser(userId)

        return try {
            supabaseClient.postgrest[table].delete {
                filter {
                    eq("user_id", userId)
                    eq("is_checked", true)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear checked items from Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun clearAll(): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        shoppingListDao.deleteAllByUser(userId)

        return try {
            supabaseClient.postgrest[table].delete {
                filter {
                    eq("user_id", userId)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear shopping list from Supabase")
            AppResult.Success(Unit)
        }
    }

    override suspend fun syncShoppingList(): AppResult<Unit> {
        val userId = authRepository.getCurrentUserId()
            ?: return AppResult.Error(AppError.AuthError("User not authenticated"))

        return try {
            val response = supabaseClient.postgrest[table]
                .select(Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ShoppingListItemDto>()

            shoppingListDao.deleteAllByUser(userId)
            shoppingListDao.insertAll(response.map { it.toShoppingListItem().toEntity() })

            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync shopping list")
            AppResult.Error(AppError.NetworkError(cause = e))
        }
    }
}
