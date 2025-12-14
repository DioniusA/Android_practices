package com.example.recipeplanner.domain.usecase.favorite

import com.example.recipeplanner.domain.model.Ingredient
import com.example.recipeplanner.domain.model.Recipe
import com.example.recipeplanner.domain.repository.FavoriteRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    private val testRecipe = Recipe(
        id = "recipe-1",
        name = "Test Recipe",
        category = "Test Category",
        area = "Test Area",
        instructions = "Test instructions",
        thumbnailUrl = "https://example.com/image.jpg",
        youtubeUrl = null,
        ingredients = listOf(
            Ingredient("Ingredient 1", "100g"),
            Ingredient("Ingredient 2", "200ml")
        ),
        tags = listOf("tag1", "tag2"),
        sourceUrl = null
    )

    @Before
    fun setup() {
        favoriteRepository = mockk()
        toggleFavoriteUseCase = ToggleFavoriteUseCase(favoriteRepository)
    }

    @Test
    fun `invoke adds recipe to favorites when not favorited`() = runTest {
        // Given
        coEvery { favoriteRepository.toggleFavorite(testRecipe) } returns AppResult.Success(true)

        // When
        val result = toggleFavoriteUseCase(testRecipe)

        // Then
        assertTrue(result is AppResult.Success)
        assertEquals(true, (result as AppResult.Success).data)
        coVerify { favoriteRepository.toggleFavorite(testRecipe) }
    }

    @Test
    fun `invoke removes recipe from favorites when already favorited`() = runTest {
        // Given
        coEvery { favoriteRepository.toggleFavorite(testRecipe) } returns AppResult.Success(false)

        // When
        val result = toggleFavoriteUseCase(testRecipe)

        // Then
        assertTrue(result is AppResult.Success)
        assertEquals(false, (result as AppResult.Success).data)
        coVerify { favoriteRepository.toggleFavorite(testRecipe) }
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        val expectedError = AppError.AuthError("User not authenticated")
        coEvery { favoriteRepository.toggleFavorite(testRecipe) } returns AppResult.Error(expectedError)

        // When
        val result = toggleFavoriteUseCase(testRecipe)

        // Then
        assertTrue(result is AppResult.Error)
        assertEquals(expectedError, (result as AppResult.Error).error)
    }

    @Test
    fun `invoke returns error when network fails`() = runTest {
        // Given
        val expectedError = AppError.NetworkError("No internet connection")
        coEvery { favoriteRepository.toggleFavorite(testRecipe) } returns AppResult.Error(expectedError)

        // When
        val result = toggleFavoriteUseCase(testRecipe)

        // Then
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.NetworkError)
    }
}
