package com.example.recipeplanner.domain.usecase.shoppinglist

import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

/**
 * Use case for toggling the checked state of a shopping list item.
 */
class ToggleShoppingItemUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) {
    suspend operator fun invoke(itemId: String): AppResult<Unit> {
        return shoppingListRepository.toggleItemChecked(itemId)
    }
}
