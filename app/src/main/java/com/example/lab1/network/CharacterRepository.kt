package com.example.lab1.network

import com.example.lab1.Extentions.Character
import com.example.lab1.database.AppDatabase
import com.example.lab1.database.CharacterDao
import com.example.lab1.database.LastIdEntity
import com.example.lab1.database.toDomain
import com.example.lab1.database.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class CharacterRepository(
    private val apiService: GoTApiService,
    private val characterDao: CharacterDao
) {

    suspend fun initializeData(): List<Character> {
        return withContext(Dispatchers.IO) {
            // Проверяем, есть ли уже данные в базе
            if (characterDao.hasAnyCharacters()) {
                Timber.d("Данные уже есть в базе, пропускаем инициализацию")
                return@withContext characterDao.getAllCharactersSync().map { it.toDomain() }
            }

            Timber.d("Инициализация данных - загрузка первых $INITIAL_LOAD_COUNT персонажей")

            // Загружаем первые 50 персонажей
            val characters = getCharactersInRange(1, INITIAL_LOAD_COUNT)

            // Сохраняем в базу данных
            saveCharactersToDatabase(characters)

            characters
        }
    }

    suspend fun getCharactersForDisplay(limit: Int, offset: Int): List<Character> {
        return try {
            val characters = characterDao.getCharactersWithPaging(limit, offset).map { it.toDomain() }
            if (characters.isEmpty()) {
                Timber.w("Получен пустой список персонажей для limit=$limit, offset=$offset")
            }
            characters
        } catch (e: Exception) {
            Timber.e(e, "Ошибка получения персонажей из базы данных")
            emptyList() // Возвращаем пустой список вместо падения
        }
    }

    suspend fun loadMoreCharacters(startId: Int, count: Int): List<Character> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Загрузка дополнительных персонажей с ID $startId, количество: $count")
                val characters = getCharactersInRange(startId, startId + count - 1)

                if (characters.isEmpty()) {
                    Timber.w("API вернул пустой список для диапазона $startId-${startId + count - 1}")
                    return@withContext emptyList()
                }

                // Сохраняем новых персонажей в базу
                var savedCount = 0
                characters.forEach { character ->
                    try {
                        characterDao.insertCharacter(character.toEntity(1))
                        savedCount++
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка сохранения персонажа с ID ${character.id}")
                    }
                }

                Timber.d("Сохранено в БД: $savedCount из ${characters.size} персонажей")

                // Возвращаем только успешно сохраненные персонажи
                characters.take(savedCount)
            } catch (e: Exception) {
                Timber.e(e, "Критическая ошибка загрузки дополнительных персонажей")
                emptyList()
            }
        }
    }

    suspend fun loadAdditionalCharacters(startId: Int, count: Int): List<Character> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Загрузка дополнительных персонажей с ID $startId, количество: $count")
                val characters = getCharactersInRange(startId, startId + count - 1)

                if (characters.isEmpty()) {
                    Timber.w("API вернул пустой список для диапазона $startId-${startId + count - 1}")
                    return@withContext emptyList()
                }

                // Сохраняем новых персонажей в базу
                characters.forEach { character ->
                    try {
                        characterDao.insertCharacter(character.toEntity(1))
                    } catch (e: Exception) {
                        Timber.e(e, "Ошибка сохранения персонажа с ID ${character.id}")
                    }
                }

                Timber.d("Успешно загружено и сохранено: ${characters.size} персонажей")
                characters
            } catch (e: Exception) {
                Timber.e(e, "Критическая ошибка загрузки дополнительных персонажей")
                emptyList()
            }
        }
    }

    /**
     * Получение последнего ID персонажа в базе
     */
    suspend fun getLastCharacterId(): Int {
        return characterDao.getLastCharacterId()
    }

    suspend fun getCharactersInRange(startId: Int, endId: Int): List<Character> = withContext(Dispatchers.IO) {
        val characters = mutableListOf<Character>()
        var successfulCount = 0
        var failedCount = 0

        for (id in startId..endId) {
            try {
                val character = apiService.getCharacter(id)
                if (character.name.isNotEmpty()) {
                    characters.add(character)
                    successfulCount++
                } else {
                    Timber.w("Пустое имя для персонажа с ID $id")
                    failedCount++
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки персонажа $id")
                failedCount++
                continue
            }
        }

        Timber.d("Загружено: $successfulCount персонажей, ошибок: $failedCount")
        return@withContext characters
    }

    private suspend fun saveCharactersToDatabase(characters: List<Character>) {
        try {
            val entities = characters.map { character ->
                character.toEntity(1) // rangeId = 1
            }
            characterDao.insertAll(entities)
            Timber.d("Сохранено ${entities.size} персонажей в базу данных")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка сохранения персонажей в базу данных")
        }
    }

    suspend fun saveCharacterToDatabase(character: Character) {
        try {
            characterDao.insertCharacter(character.toEntity(1))
            Timber.d("Сохранен персонаж с ID ${character.id}")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка сохранения персонажа с ID ${character.id}")
        }
    }

    suspend fun refreshAllData(): List<Character> {
        return withContext(Dispatchers.IO) {
            try {
                // Очищаем базу
                characterDao.clearAll()

                // Загружаем первые 50 персонажей
                val characters = getCharactersInRange(1, INITIAL_LOAD_COUNT)

                // Сохраняем в базу
                saveCharactersToDatabase(characters)

                // Обновляем последний ID
                val lastId = characters.maxByOrNull { it.id }?.id ?: INITIAL_LOAD_COUNT
                characterDao.insertLastId(LastIdEntity(lastId = lastId))

                Timber.d("База данных обновлена. Сохранено: ${characters.size} персонажей")
                characters
            } catch (e: Exception) {
                Timber.e(e, "Ошибка обновления базы данных")
                emptyList()
            }
        }
    }

    suspend fun getTotalCharactersCount(): Int {
        return characterDao.countAllCharacters()
    }

    suspend fun hasDataInDatabase(): Boolean {
        return characterDao.hasAnyCharacters()
    }


    /**
     * Получить общее количество персонажей как Flow
     */
    fun countAllCharactersFlow(): Flow<Int> {
        return characterDao.countAllCharactersFlow()
    }

    fun getAllCharactersFlow(): Flow<List<Character>> {
        return characterDao.getAllCharactersFlow()
            .map { entities -> entities.map { it.toDomain() } }
    }

    companion object {

        private const val INITIAL_LOAD_COUNT = 20

        @Volatile
        private var instance: CharacterRepository? = null

        fun getInstance(context: android.content.Context): CharacterRepository {
            return instance ?: synchronized(this) {
                instance ?: CharacterRepository(
                    GoTApiService.create(),
                    AppDatabase.getDatabase(context).characterDao()
                ).also { instance = it }
            }
        }
    }
}