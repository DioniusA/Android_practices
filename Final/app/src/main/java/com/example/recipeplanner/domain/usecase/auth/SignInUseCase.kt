package com.example.recipeplanner.domain.usecase.auth

import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

/**
 * Use case for signing in a user.
 */
class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<User> {
        // Validate input
        if (email.isBlank()) {
            return AppResult.Error(AppError.InvalidCredentials("Email cannot be empty"))
        }
        if (!email.contains("@")) {
            return AppResult.Error(AppError.InvalidCredentials("Invalid email format"))
        }
        if (password.isBlank()) {
            return AppResult.Error(AppError.InvalidCredentials("Password cannot be empty"))
        }
        if (password.length < 6) {
            return AppResult.Error(AppError.InvalidCredentials("Password must be at least 6 characters"))
        }

        return authRepository.signIn(email.trim(), password)
    }
}
