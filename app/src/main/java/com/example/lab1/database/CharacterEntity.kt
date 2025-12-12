package com.example.lab1.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lab1.Extentions.Character

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val culture: String?,
    val born: String?,
    val titles: String, // Храним как разделенную запятыми строку
    val aliases: String, // Храним как разделенную запятыми строку
    val playedBy: String, // Храним как разделенную запятыми строку
    val rangeId: Int // ID диапазона (1 для 1-50, 2 для 51-100 и т.д.)
)

// Extension functions для конвертации
fun Character.toEntity(rangeId: Int): CharacterEntity {
    return CharacterEntity(
        id = this.id,
        name = this.name,
        culture = this.culture,
        born = this.born,
        titles = this.titles.joinToString(","),
        aliases = this.aliases.joinToString(","),
        playedBy = this.playedBy.joinToString(","),
        rangeId = rangeId
    )
}

fun CharacterEntity.toDomain(): Character {
    return Character(
        name = this.name,
        culture = this.culture,
        born = this.born,
        titles = this.titles.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        aliases = this.aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        playedBy = this.playedBy.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        url = "https://www.anapioficeandfire.com/api/characters/$id",
    )
}