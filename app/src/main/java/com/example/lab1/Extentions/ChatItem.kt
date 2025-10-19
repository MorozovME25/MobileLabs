package com.example.lab1.Extentions

import com.example.lab1.R

data class ChatItem(
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val avatarResId: Int = R.drawable.ic_launcher_foreground
)