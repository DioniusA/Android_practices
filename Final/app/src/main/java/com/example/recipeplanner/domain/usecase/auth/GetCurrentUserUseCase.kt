package com.example.recipeplanner.domain.usecase.auth

import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the current user.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> = authRepository.currentUser
}
