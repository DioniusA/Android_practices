package com.example.recipeplanner.domain.usecase.shoppinglist

import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the shopping list.
 */
class GetShoppingListUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) {
    operator fun invoke(): Flow<List<ShoppingListItem>> {
        return shoppingListRepository.getShoppingList()
    }
}
