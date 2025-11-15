package com.example.practice_7.data

import com.example.practice_7.ApiService
import com.example.practice_7.UserDao
import com.example.practice_7.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService
) {
    suspend fun getUser(): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUser()
    }

    suspend fun insertUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun getUsersFromApi(): List<com.example.practice_7.ApiUser> = withContext(Dispatchers.IO) {
        apiService.getUsers()
    }
}

