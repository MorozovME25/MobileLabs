package com.example.lab1.Fragments

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab1.Extentions.BaseActivity
import com.example.lab1.Extentions.ChatAdapter
import com.example.lab1.Extentions.ChatItem
import com.example.lab1.R

class HomeActivity : BaseActivity() {
    private lateinit var adapter: ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val recyclerView = findViewById<RecyclerView>(R.id.recycle)
        val chatList = listOf(
            ChatItem("Анна", "Привет! Как дела?", "12:30"),
            ChatItem("Иван", "Встреча в 15:00", "11:45"),
            ChatItem("Команда", "Новый проект запущен", "10:20"),
            ChatItem("Мария", "Спасибо за помощь!", "Вчера"),
            ChatItem("Алексей", "Готов к презентации", "Вчера")
        )

        val adapter = ChatAdapter(chatList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}