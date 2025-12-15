package com.example.recipeplanner.data.repository

import com.example.recipeplanner.domain.model.User
import com.example.recipeplanner.domain.repository.AuthRepository
import com.example.recipeplanner.domain.util.AppError
import com.example.recipeplanner.domain.util.AppResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    private val auth: Auth get() = supabaseClient.auth

    override val currentUser: Flow<User?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = status.session.user
                user?.let {
                    User(
                        id = it.id,
                        email = it.email ?: "",
                        displayName = it.userMetadata?.get("display_name")?.toString()
                    )
                }
            }
            else -> null
        }
    }

    override val isAuthenticated: Flow<Boolean> = auth.sessionStatus.map { status ->
        status is SessionStatus.Authenticated
    }

    override suspend fun signIn(email: String, password: String): AppResult<User> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val session = auth.currentSessionOrNull()
            val user = session?.user
            if (user != null) {
                AppResult.Success(
                    User(
                        id = user.id,
                        email = user.email ?: email,
                        displayName = user.userMetadata?.get("display_name")?.toString()
                    )
                )
            } else {
                AppResult.Error(AppError.AuthError("Failed to get user after sign in"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Sign in failed")
            val error = when {
                e.message?.contains("Invalid login credentials") == true -> AppError.InvalidCredentials()
                e.message?.contains("Email not confirmed") == true -> AppError.AuthError("Please confirm your email")
                else -> AppError.AuthError(e.message ?: "Sign in failed")
            }
            AppResult.Error(error)
        }
    }

    override suspend fun signUp(email: String, password: String): AppResult<User> {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val session = auth.currentSessionOrNull()
            val user = session?.user
            if (user != null) {
                AppResult.Success(
                    User(
                        id = user.id,
                        email = user.email ?: email,
                        displayName = null
                    )
                )
            } else {
                AppResult.Error(AppError.AuthError("Please check your email to confirm your account"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Sign up failed")
            val error = when {
                e.message?.contains("already registered") == true -> AppError.EmailAlreadyExists()
                else -> AppError.AuthError(e.message ?: "Sign up failed")
            }
            AppResult.Error(error)
        }
    }

    override suspend fun signOut(): AppResult<Unit> {
        return try {
            auth.signOut()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sign out failed")
            AppResult.Error(AppError.AuthError(e.message ?: "Sign out failed"))
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }
}
