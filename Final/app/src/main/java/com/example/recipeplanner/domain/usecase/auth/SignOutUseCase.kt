package com.example.recipeplanner.domain.usecase.auth

import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.util.AppResult
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return authRepository.signOut()
    }
}
