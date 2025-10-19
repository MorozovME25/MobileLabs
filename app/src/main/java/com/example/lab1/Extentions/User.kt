package com.example.lab1.Extentions

import java.io.Serializable

data class User(
    val name: String,
    val email: String,
    val password: String
) : Serializable