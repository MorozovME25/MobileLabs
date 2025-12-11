package com.example.lab1.network

import com.example.lab1.Extentions.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class CharacterRepository(private val apiService: GoTApiService) {

    private val characterCache = mutableMapOf<Int, Character>()

    // Ограничение количества одновременных запросов (защита от перегрузки)
    private val requestSemaphore = Semaphore(10) // Максимум 10 одновременных запросов

    // Таймаут для одного запроса

    /**
     * Оптимизированный метод загрузки персонажей в диапазоне
     * Загружает персонажей параллельно с ограничением количества одновременных запросов
     */
    suspend fun getCharactersInRange(startId: Int, endId: Int): List<Character> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        Timber.d("Начало загрузки персонажей в диапазоне $startId-$endId")

        // Фильтруем уже загруженные из кэша
        val idsToLoad = (startId..endId).filter { it !in characterCache }
        val cachedCharacters = (startId..endId).filter { it in characterCache }.map { characterCache[it]!! }

        Timber.d("Кэш содержит ${cachedCharacters.size} персонажей, нужно загрузить ${idsToLoad.size}")

        if (idsToLoad.isEmpty()) {
            Timber.d("Все персонажи найдены в кэше")
            return@withContext cachedCharacters
        }

        // Параллельная загрузка с ограничением
        val loadedCharacters = coroutineScope {
            idsToLoad.map { characterId ->
                async {
                    loadCharacterWithRetry(characterId)
                }
            }.awaitAll().filterNotNull()
        }

        // Сохраняем в кэш
        loadedCharacters.forEach { character ->
            characterCache[character.id] = character
        }

        val totalTime = System.currentTimeMillis() - startTime
        Timber.d("Загрузка завершена за $totalTime мс. Загружено: ${loadedCharacters.size}, из кэша: ${cachedCharacters.size}")

        (cachedCharacters + loadedCharacters).sortedBy { it.id }
    }

    /**
     * Загрузка одного персонажа с ретраями и таймаутом
     */
    private suspend fun loadCharacterWithRetry(characterId: Int, maxRetries: Int = 3): Character? {
        var attempt = 0

        while (attempt < maxRetries) {
            attempt++
            try {
                // Запрашиваем разрешение на выполнение запроса
                return requestSemaphore.withPermit {
                    Timber.d("Запрос персонажа $characterId (попытка $attempt/$maxRetries)")
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        val character = apiService.getCharacter(characterId)

                        // ИСПРАВЛЕНО: добавляем ID для сортировки
                        val characterWithId = character.copy(id = characterId)

                        // Проверяем валидность данных
                        if (characterWithId.name.isNotEmpty()) {
                            characterWithId
                        } else {
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки персонажа $characterId на попытке $attempt")

                // Если это последняя попытка - пробрасываем ошибку или возвращаем null
                if (attempt == maxRetries) {
                    return null
                }

                // Задержка перед повторной попыткой (экспоненциальная)
                delay(30L * attempt)
            }
        }
        return null
    }

    /**
     * Очистка кэша при необходимости
     */
    fun clearCache() {
        characterCache.clear()
        Timber.d("Кэш персонажей очищен")
    }

    companion object {
        private const val REQUEST_TIMEOUT_MS = 5000L
        @Volatile
        private var instance: CharacterRepository? = null

        fun getInstance(): CharacterRepository {
            return instance ?: synchronized(this) {
                instance ?: CharacterRepository(GoTApiService.create()).also { instance = it }
            }
        }
    }
}