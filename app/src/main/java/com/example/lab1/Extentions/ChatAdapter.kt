package com.example.lab1.Extentions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lab1.R
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(private val chatList: List<ChatItem>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: CircleImageView = itemView.findViewById(R.id.ivAvatar)
        val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.ivAvatar.setImageResource(chat.avatarResId)
        holder.tvSenderName.text = chat.senderName
        holder.tvLastMessage.text = chat.lastMessage
        holder.tvTime.text = chat.time
    }

    override fun getItemCount() = chatList.size
}