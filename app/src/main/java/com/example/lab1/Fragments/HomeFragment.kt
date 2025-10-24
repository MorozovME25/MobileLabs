package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.Extentions.ChatAdapter
import com.example.lab1.Extentions.ChatItem
import com.example.lab1.R

class HomeFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_home, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycle)
        val chatList = listOf(
            ChatItem("Анна", "Привет! Как дела?", "12:30"),
            ChatItem("Иван", "Встреча в 15:00", "11:45"),
            ChatItem("Команда", "Новый проект запущен", "10:20"),
            ChatItem("Мария", "Спасибо за помощь!", "Вчера"),
            ChatItem("Алексей", "Готов к презентации", "Вчера")
        )

        val adapter = ChatAdapter(chatList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return view
    }
}