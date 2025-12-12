package com.example.lab1.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {


    @Query("SELECT * FROM characters ORDER BY id ASC")
    suspend fun getAllCharactersSync(): List<CharacterEntity>

    @Query("SELECT * FROM characters ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getCharactersWithPaging(limit: Int, offset: Int): List<CharacterEntity>

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun countAllCharacters(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters")
    suspend fun clearAll()

    @Query("SELECT EXISTS(SELECT 1 FROM characters LIMIT 1)")
    suspend fun hasAnyCharacters(): Boolean

    @Query("SELECT MAX(id) FROM characters")
    suspend fun getLastCharacterId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastId(lastId: LastIdEntity)

//    @Query("SELECT * FROM characters ORDER BY id ASC")
//    fun getAllCharactersFlow(): Flow<List<CharacterEntity>>
//
//    // Реактивный запрос с пагинацией
//    @Query("SELECT * FROM characters ORDER BY id ASC LIMIT :limit OFFSET :offset")
//    fun getCharactersWithPagingFlow(limit: Int, offset: Int): Flow<List<CharacterEntity>>
//
//    @Query("SELECT * FROM characters ORDER BY id ASC")
//    fun getAllCharacters(): Flow<List<CharacterEntity>>
//
//    @Query("SELECT * FROM characters ORDER BY id ASC")
//    suspend fun getAllCharactersSync(): List<CharacterEntity>
//
//    @Query("SELECT * FROM characters ORDER BY id ASC LIMIT :limit OFFSET :offset")
//    suspend fun getCharactersWithPaging(limit: Int, offset: Int): List<CharacterEntity>
//
//    @Query("SELECT COUNT(*) FROM characters")
//    suspend fun countAllCharacters(): Int
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(characters: List<CharacterEntity>)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertCharacter(character: CharacterEntity)
//
//    @Query("DELETE FROM characters")
//    suspend fun clearAll()
//
//    @Query("SELECT EXISTS(SELECT 1 FROM characters LIMIT 1)")
//    suspend fun hasAnyCharacters(): Boolean
//
//    @Query("SELECT MAX(id) FROM characters")
//    suspend fun getLastCharacterId(): Int
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertLastId(lastId: LastIdEntity)
//
//    @Query("SELECT * FROM last_id")
//    suspend fun getLastIdRecord(): LastIdEntity?

}
// Новая сущность для хранения последнего ID
@Entity(tableName = "last_id")
data class LastIdEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "last_id")
    val lastId: Int
)