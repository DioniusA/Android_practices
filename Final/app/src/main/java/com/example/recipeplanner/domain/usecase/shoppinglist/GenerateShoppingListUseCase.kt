package com.example.recipeplanner.domain.usecase.shoppinglist

import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.IngredientCategory
import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.util.AppResult
import java.util.UUID
import javax.inject.Inject

class GenerateShoppingListUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository
) {
    suspend operator fun invoke(
        mealPlanEntries: List<MealPlanEntry>,
        userId: String
    ): AppResult<List<ShoppingListItem>> {
        val ingredientMap = mutableMapOf<String, AggregatedIngredient>()

        for (entry in mealPlanEntries) {
            val recipeResult = recipeRepository.getRecipeById(entry.recipeId)
            if (recipeResult is AppResult.Success) {
                val recipe = recipeResult.data
                for (ingredient in recipe.ingredients) {
                    val key = ingredient.name.lowercase().trim()
                    val existing = ingredientMap[key]
                    if (existing != null) {
                        ingredientMap[key] = existing.copy(
                            quantities = existing.quantities + ingredient.measure,
                            recipeIds = existing.recipeIds + entry.recipeId
                        )
                    } else {
                        ingredientMap[key] = AggregatedIngredient(
                            name = ingredient.name,
                            quantities = listOf(ingredient.measure),
                            recipeIds = listOf(entry.recipeId)
                        )
                    }
                }
            }
        }

        val items = ingredientMap.values.map { agg ->
            ShoppingListItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                ingredientName = agg.name,
                quantity = agg.quantities.distinct().joinToString(" + "),
                category = IngredientCategory.fromIngredientName(agg.name),
                isChecked = false,
                recipeIds = agg.recipeIds.distinct()
            )
        }.sortedWith(compareBy({ it.category }, { it.ingredientName }))

        val saveResult = shoppingListRepository.addItems(items)
        return saveResult.map { items }
    }

    private data class AggregatedIngredient(
        val name: String,
        val quantities: List<String>,
        val recipeIds: List<String>
    )
}
