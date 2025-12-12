package com.example.lab1.Extentions

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lab1.databinding.ItemCharacterBinding
import com.example.lab1.databinding.ItemFooterBinding
import timber.log.Timber

class CharacterAdapter(
    private var characters: List<Character>,
    private var hasMoreData: Boolean = true,
    private var isLoadingMore: Boolean = false,
    private val onLoadMore: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CHARACTER = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == characters.size) VIEW_TYPE_FOOTER else VIEW_TYPE_CHARACTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CHARACTER -> {
                val binding = ItemCharacterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                CharacterViewHolder(binding)
            }
            VIEW_TYPE_FOOTER -> {
                val binding = ItemFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                FooterViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CharacterViewHolder -> holder.bind(characters[position])
            is FooterViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int {
        // Всегда показываем footer, если есть данные или идет загрузка
        return characters.size + if (characters.isNotEmpty() || isLoadingMore || hasMoreData) 1 else 0
    }

    inner class CharacterViewHolder(private val binding: ItemCharacterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(character: Character) {
            binding.apply {
                tvCharacterName.text = character.name.ifEmpty { "Без имени" }
                tvCharacterCulture.text = "Культура: ${character.culture ?: "Неизвестно"}"
                tvCharacterBorn.text = "Родился: ${character.born ?: "Неизвестно"}"

                val titlesText = if (character.titles.isNotEmpty()) {
                    "Титулы: ${character.titles.joinToString(", ").ifEmpty { "Отсутствуют" }}"
                } else {
                    "Титулы: Отсутствуют"
                }
                tvCharacterTitles.text = titlesText

                val aliasesText = if (character.aliases.isNotEmpty()) {
                    "Псевдонимы: ${character.aliases.joinToString(", ").ifEmpty { "Отсутствуют" }}"
                } else {
                    "Псевдонимы: Отсутствуют"
                }
                tvCharacterAliases.text = aliasesText

                val playedByText = if (character.playedBy.isNotEmpty()) {
                    "Играет: ${character.playedBy.joinToString(", ").ifEmpty { "Не снимался в сериале" }}"
                } else {
                    "Играет: Не снимался в сериале"
                }
                tvCharacterPlayedBy.text = playedByText
            }
        }
    }

    inner class FooterViewHolder(private val binding: ItemFooterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.apply {
                tvLoadMoreInfo.visibility = if (hasMoreData) View.VISIBLE else View.GONE

                if (hasMoreData) {
                    tvLoadMoreInfo.text = "Можно загрузить еще персонажей"
                } else {
                    tvLoadMoreInfo.text = "Все персонажи загружены"
                }

                btnLoadMore.visibility = if (hasMoreData && !isLoadingMore) View.VISIBLE else View.GONE
                progressBarFooter.visibility = if (isLoadingMore) View.VISIBLE else View.GONE

                btnLoadMore.setOnClickListener {
                    onLoadMore()
                }

                Timber.d("Состояние footer: hasMoreData=$hasMoreData, isLoadingMore=$isLoadingMore, btnVisibility=${btnLoadMore.visibility}")
            }
        }
    }

    // Обновить все данные
    fun updateData(newCharacters: List<Character>) {
        characters = newCharacters
        notifyDataSetChanged()
    }

    // Добавить новые элементы в конец списка
    fun addItems(newCharacters: List<Character>) {
        if (newCharacters.isEmpty()) return
        val startPosition = characters.size
        characters = characters + newCharacters
        notifyItemRangeInserted(startPosition, newCharacters.size)
    }

    // Обновить состояние загрузки
    fun updateLoadingState(isLoading: Boolean, hasMore: Boolean) {
        val wasLoading = isLoadingMore
        val wasHasMore = hasMoreData

        isLoadingMore = isLoading
        hasMoreData = hasMore

        notifyItemChanged(characters.size)
    }

    // Очистить список
    fun clearData() {
        characters = emptyList()
        notifyDataSetChanged()
    }
}