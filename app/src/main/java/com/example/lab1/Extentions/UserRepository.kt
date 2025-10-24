package com.example.lab1.Extentions

object UserRepository {
    private val registeredUsers = mutableSetOf<User>()

    fun register(user: User) {
        registeredUsers.add(user)
    }

    fun isValidCredentials(email: String, password: String): Boolean {
        return registeredUsers.any { it.email == email && it.password == password }
    }

    fun isEmailRegistered(email: String): Boolean {
        return registeredUsers.any { it.email == email }
    }
}