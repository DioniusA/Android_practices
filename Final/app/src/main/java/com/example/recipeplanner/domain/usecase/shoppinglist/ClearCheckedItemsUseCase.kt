package com.example.recipeplanner.domain.usecase.shoppinglist

import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

class ClearCheckedItemsUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return shoppingListRepository.clearCheckedItems()
    }
}
