package com.example.recipeplanner.domain.usecase.auth

import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignInUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var signInUseCase: SignInUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        signInUseCase = SignInUseCase(authRepository)
    }

    @Test
    fun `invoke with valid credentials returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(id = "1", email = email, displayName = null)
        coEvery { authRepository.signIn(email, password) } returns AppResult.Success(expectedUser)

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Success)
        assertEquals(expectedUser, (result as AppResult.Success).data)
    }

    @Test
    fun `invoke with empty email returns error`() = runTest {
        // Given
        val email = ""
        val password = "password123"

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.InvalidCredentials)
    }

    @Test
    fun `invoke with invalid email format returns error`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.InvalidCredentials)
    }

    @Test
    fun `invoke with empty password returns error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = ""

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.InvalidCredentials)
    }

    @Test
    fun `invoke with short password returns error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "12345"

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Error)
        val error = (result as AppResult.Error).error
        assertTrue(error is AppError.InvalidCredentials)
        assertTrue(error.message.contains("at least 6 characters"))
    }

    @Test
    fun `invoke trims email before calling repository`() = runTest {
        // Given
        val email = "  test@example.com  "
        val password = "password123"
        val expectedUser = User(id = "1", email = "test@example.com", displayName = null)
        coEvery { authRepository.signIn("test@example.com", password) } returns AppResult.Success(expectedUser)

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `invoke with repository error returns error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedError = AppError.InvalidCredentials("Wrong password")
        coEvery { authRepository.signIn(email, password) } returns AppResult.Error(expectedError)

        // When
        val result = signInUseCase(email, password)

        // Then
        assertTrue(result is AppResult.Error)
        assertEquals(expectedError, (result as AppResult.Error).error)
    }
}
