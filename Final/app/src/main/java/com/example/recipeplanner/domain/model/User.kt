package com.example.recipeplanner.domain.model

/**
 * Domain model representing an authenticated user.
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String?
)
