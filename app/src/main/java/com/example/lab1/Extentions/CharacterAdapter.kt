package com.example.lab1.Extentions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lab1.databinding.ItemCharacterBinding

class CharacterAdapter(private var characters: List<Character>) :
    RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Character) {
            binding.apply {
                tvCharacterName.text = character.name.ifEmpty { "Без имени" }
                tvCharacterCulture.text = "Культура: ${character.culture ?: "Неизвестно"}"
                tvCharacterBorn.text = "Родился: ${character.born ?: "Неизвестно"}"

                val titlesText = if (character.titles.isNotEmpty()) {
                    "Титулы: ${character.titles.joinToString(", ")}"
                } else {
                    "Титулы: Отсутствуют"
                }
                tvCharacterTitles.text = titlesText

                val aliasesText = if (character.aliases.isNotEmpty()) {
                    "Псевдонимы: ${character.aliases.joinToString(", ")}"
                } else {
                    "Псевдонимы: Отсутствуют"
                }
                tvCharacterAliases.text = aliasesText

                val playedByText = if (character.playedBy.isNotEmpty()) {
                    "Играет: ${character.playedBy.joinToString(", ")}"
                } else {
                    "Играет: Не снимался в сериале"
                }
                tvCharacterPlayedBy.text = playedByText
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemCharacterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(characters[position])
    }

    override fun getItemCount() = characters.size

    fun updateData(newCharacters: List<Character>) {
        characters = newCharacters
        notifyDataSetChanged()
    }
}