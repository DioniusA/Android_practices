package com.example.recipeplanner.domain.usecase.shoppinglist

import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.IngredientCategory
import com.example.recipeplanner.domain.model.MealPlanEntry
import com.example.recipeplanner.domain.model.MealType
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.model.ShoppingListItem
import com.example.recipeplanner.domain.repository.RecipeRepository
import com.example.recipeplanner.domain.repository.ShoppingListRepository
import com.example.recipeplanner.domain.util.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GenerateShoppingListUseCaseTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var shoppingListRepository: ShoppingListRepository
    private lateinit var generateShoppingListUseCase: GenerateShoppingListUseCase

    private val userId = "user-123"

    @Before
    fun setup() {
        recipeRepository = mockk()
        shoppingListRepository = mockk()
        generateShoppingListUseCase = GenerateShoppingListUseCase(recipeRepository, shoppingListRepository)
    }

    @Test
    fun `invoke generates shopping list from meal plan entries`() = runTest {
        // Given
        val recipe1 = createTestRecipe(
            id = "r1",
            ingredients = listOf(
                Ingredient("Chicken", "500g"),
                Ingredient("Onion", "2 medium")
            )
        )
        val recipe2 = createTestRecipe(
            id = "r2",
            ingredients = listOf(
                Ingredient("Pasta", "400g"),
                Ingredient("Onion", "1 large")
            )
        )

        val mealPlanEntries = listOf(
            createMealPlanEntry("m1", "r1"),
            createMealPlanEntry("m2", "r2")
        )

        coEvery { recipeRepository.getRecipeById("r1") } returns AppResult.Success(recipe1)
        coEvery { recipeRepository.getRecipeById("r2") } returns AppResult.Success(recipe2)
        
        val itemsSlot = slot<List<ShoppingListItem>>()
        coEvery { shoppingListRepository.addItems(capture(itemsSlot)) } returns AppResult.Success(Unit)

        // When
        val result = generateShoppingListUseCase(mealPlanEntries, userId)

        // Then
        assertTrue(result is AppResult.Success)
        val items = (result as AppResult.Success).data
        
        // Should have 3 unique ingredients (Chicken, Onion merged, Pasta)
        assertEquals(3, items.size)
        
        // Onion should be merged from both recipes
        val onionItem = items.find { it.ingredientName.lowercase() == "onion" }
        assertTrue(onionItem != null)
        assertTrue(onionItem!!.recipeIds.containsAll(listOf("r1", "r2")))
        
        coVerify { shoppingListRepository.addItems(any()) }
    }

    @Test
    fun `invoke categorizes ingredients correctly`() = runTest {
        // Given
        val recipe = createTestRecipe(
            id = "r1",
            ingredients = listOf(
                Ingredient("Chicken breast", "500g"),
                Ingredient("Tomato", "3"),
                Ingredient("Milk", "1 cup"),
                Ingredient("Salt", "1 tsp")
            )
        )

        val mealPlanEntries = listOf(createMealPlanEntry("m1", "r1"))

        coEvery { recipeRepository.getRecipeById("r1") } returns AppResult.Success(recipe)
        coEvery { shoppingListRepository.addItems(any()) } returns AppResult.Success(Unit)

        // When
        val result = generateShoppingListUseCase(mealPlanEntries, userId)

        // Then
        assertTrue(result is AppResult.Success)
        val items = (result as AppResult.Success).data

        val chickenItem = items.find { it.ingredientName == "Chicken breast" }
        val tomatoItem = items.find { it.ingredientName == "Tomato" }
        val milkItem = items.find { it.ingredientName == "Milk" }
        val saltItem = items.find { it.ingredientName == "Salt" }

        assertEquals(IngredientCategory.MEAT, chickenItem?.category)
        assertEquals(IngredientCategory.PRODUCE, tomatoItem?.category)
        assertEquals(IngredientCategory.DAIRY, milkItem?.category)
        assertEquals(IngredientCategory.SPICES, saltItem?.category)
    }

    @Test
    fun `invoke returns empty list when no meal plan entries`() = runTest {
        // Given
        val mealPlanEntries = emptyList<MealPlanEntry>()
        coEvery { shoppingListRepository.addItems(any()) } returns AppResult.Success(Unit)

        // When
        val result = generateShoppingListUseCase(mealPlanEntries, userId)

        // Then
        assertTrue(result is AppResult.Success)
        val items = (result as AppResult.Success).data
        assertTrue(items.isEmpty())
    }

    @Test
    fun `invoke handles recipe fetch failure gracefully`() = runTest {
        // Given
        val mealPlanEntries = listOf(
            createMealPlanEntry("m1", "r1"),
            createMealPlanEntry("m2", "r2")
        )

        val recipe2 = createTestRecipe(
            id = "r2",
            ingredients = listOf(Ingredient("Pasta", "400g"))
        )

        coEvery { recipeRepository.getRecipeById("r1") } returns AppResult.Error(
            com.example.recipeplanner.domain.util.AppError.NotFound("Recipe not found")
        )
        coEvery { recipeRepository.getRecipeById("r2") } returns AppResult.Success(recipe2)
        coEvery { shoppingListRepository.addItems(any()) } returns AppResult.Success(Unit)

        // When
        val result = generateShoppingListUseCase(mealPlanEntries, userId)

        // Then
        assertTrue(result is AppResult.Success)
        val items = (result as AppResult.Success).data
        // Should only have items from the successful recipe fetch
        assertEquals(1, items.size)
        assertEquals("Pasta", items[0].ingredientName)
    }

    @Test
    fun `invoke merges quantities for same ingredient`() = runTest {
        // Given
        val recipe1 = createTestRecipe(
            id = "r1",
            ingredients = listOf(Ingredient("Flour", "200g"))
        )
        val recipe2 = createTestRecipe(
            id = "r2",
            ingredients = listOf(Ingredient("Flour", "300g"))
        )

        val mealPlanEntries = listOf(
            createMealPlanEntry("m1", "r1"),
            createMealPlanEntry("m2", "r2")
        )

        coEvery { recipeRepository.getRecipeById("r1") } returns AppResult.Success(recipe1)
        coEvery { recipeRepository.getRecipeById("r2") } returns AppResult.Success(recipe2)
        coEvery { shoppingListRepository.addItems(any()) } returns AppResult.Success(Unit)

        // When
        val result = generateShoppingListUseCase(mealPlanEntries, userId)

        // Then
        assertTrue(result is AppResult.Success)
        val items = (result as AppResult.Success).data
        assertEquals(1, items.size)
        
        val flourItem = items[0]
        assertEquals("flour", flourItem.ingredientName.lowercase())
        assertTrue(flourItem.quantity.contains("200g"))
        assertTrue(flourItem.quantity.contains("300g"))
    }

    private fun createTestRecipe(
        id: String,
        ingredients: List<Ingredient>
    ) = Recipe(
        id = id,
        name = "Test Recipe $id",
        category = "Test",
        area = "Test",
        instructions = "Test instructions",
        thumbnailUrl = "https://example.com/image.jpg",
        youtubeUrl = null,
        ingredients = ingredients,
        tags = emptyList(),
        sourceUrl = null
    )

    private fun createMealPlanEntry(
        id: String,
        recipeId: String
    ) = MealPlanEntry(
        id = id,
        userId = userId,
        recipeId = recipeId,
        recipeName = "Test Recipe",
        recipeImageUrl = "https://example.com/image.jpg",
        date = LocalDate.now(),
        mealType = MealType.DINNER
    )
}
