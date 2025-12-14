package com.example.recipeplanner.domain.util

/**
 * A sealed class representing the result of an operation.
 * Used throughout the app for consistent error handling.
 */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Error -> error
    }

    inline fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }

    companion object {
        fun <T> success(data: T): AppResult<T> = Success(data)
        fun error(error: AppError): AppResult<Nothing> = Error(error)
    }
}

/**
 * Sealed class representing different types of errors in the app.
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    // Network errors
    data class NetworkError(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class ServerError(
        val code: Int,
        override val message: String = "Server error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // Auth errors
    data class AuthError(
        override val message: String = "Authentication error",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class InvalidCredentials(
        override val message: String = "Invalid email or password"
    ) : AppError(message)

    data class UserNotFound(
        override val message: String = "User not found"
    ) : AppError(message)

    data class EmailAlreadyExists(
        override val message: String = "Email already registered"
    ) : AppError(message)

    // Data errors
    data class NotFound(
        override val message: String = "Resource not found"
    ) : AppError(message)

    data class DatabaseError(
        override val message: String = "Database error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    // Generic error
    data class Unknown(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}

/**
 * Extension function to convert a Throwable to an AppError.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is java.net.UnknownHostException -> AppError.NetworkError(cause = this)
    is java.net.SocketTimeoutException -> AppError.NetworkError("Connection timed out", this)
    is java.io.IOException -> AppError.NetworkError(cause = this)
    else -> AppError.Unknown(message ?: "Unknown error", this)
}

/**
 * Runs a suspending block and wraps the result in AppResult.
 */
suspend fun <T> runCatching(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: Exception) {
    AppResult.Error(e.toAppError())
}
