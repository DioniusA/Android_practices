package com.example.recipeplanner.domain.usecase.shoppinglist

import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

/**
 * Use case for clearing all checked items from the shopping list.
 */
class ClearCheckedItemsUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return shoppingListRepository.clearCheckedItems()
    }
}
