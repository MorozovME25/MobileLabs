package com.example.lab1.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab1.Extentions.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private var currentOffset = 0
    private var totalCountInDb = 0 // Начальное количество
    private var maxApiLimit = 2000

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isInitialLoading.value = true
            _isLoading.value = true
            _error.value = null

            try {
                // Загружаем только первые 20 персонажей
                loadCharacters(20, 0)

                // Получаем общее количество в базе
                totalCountInDb = repository.getTotalCharactersCount()
                Timber.d("Всего персонажей в базе: $totalCountInDb")

                // Обновляем состояние - кнопка должна быть видна, если есть данные в API
                updateHasMoreDataState()

            } catch (e: Exception) {
                Timber.e(e, "Ошибка инициализации данных")
                _error.value = "Ошибка инициализации данных: ${e.message}"
            } finally {
                _isInitialLoading.value = false
                _isLoading.value = false
            }
        }
    }

    // Загрузка следующей порции данных (всегда 10 персонажей)
    fun loadMoreCharacters() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Timber.d("Начало загрузки следующей порции. Current offset: $currentOffset")

                // Если есть данные в базе для отображения
                if (currentOffset < totalCountInDb) {
                    val newCharacters = repository.getCharactersForDisplay(10, currentOffset)

                    if (newCharacters.isNotEmpty()) {
                        val updatedList = _characters.value.toMutableList()
                        updatedList.addAll(newCharacters)
                        _characters.value = updatedList

                        currentOffset += newCharacters.size
                        Timber.d("Загружено из базы: ${newCharacters.size}, new offset: $currentOffset")
                    }
                }
                // Если данных в базе недостаточно, загружаем из API
                else {
                    Timber.w("Данных в базе недостаточно. Загружаем из API...")
                    loadFromApi()
                }

                // Всегда обновляем состояние после загрузки
                updateHasMoreDataState()

            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки данных")
                _error.value = "Ошибка загрузки данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadFromApi() {
        try {
            // Получаем последний ID из базы
            val lastId = repository.getLastCharacterId()
            val nextStartId = lastId + 1

            // Загружаем 10 персонажей из API
            val newCharacters = repository.loadMoreCharacters(nextStartId, 10)

            if (newCharacters.isEmpty()) {
                Timber.w("API вернул пустой результат. Больше данных нет.")
                return
            }

            // Сохраняем новые персонажи в базу
            newCharacters.forEach { character ->
                repository.saveCharacterToDatabase(character)
            }

            // Обновляем общее количество в базе
            totalCountInDb = repository.getTotalCharactersCount()

            // Добавляем к существующим данным
            val updatedList = _characters.value.toMutableList()
            updatedList.addAll(newCharacters)
            _characters.value = updatedList

            currentOffset += newCharacters.size

            Timber.d("Загружено из API: ${newCharacters.size}, new offset: $currentOffset, total in DB: $totalCountInDb")

        } catch (e: Exception) {
            Timber.e(e, "Ошибка загрузки из API")
            throw e
        }
    }

    private fun updateHasMoreDataState() {
        viewModelScope.launch {
            // Проверяем, есть ли еще данные в API для загрузки
            val hasMoreInApi = currentOffset < maxApiLimit

            // Проверяем, есть ли еще данные в базе
            val hasMoreInDb = currentOffset < totalCountInDb

            // Кнопка должна быть видна, если есть данные в базе ИЛИ в API
            val hasMoreData = hasMoreInDb || hasMoreInApi

            _hasMoreData.value = hasMoreData
            Timber.d("Обновление состояния: currentOffset=$currentOffset, totalCountInDb=$totalCountInDb, maxApiLimit=$maxApiLimit, hasMoreData=$hasMoreData")
        }
    }

    // Загрузка определенного количества персонажей с заданного offset
    private suspend fun loadCharacters(limit: Int, offset: Int) {
        val characters = repository.getCharactersForDisplay(limit, offset)
        _characters.value = characters
        currentOffset = offset + limit
        Timber.d("Загружено: ${characters.size}, Offset: $currentOffset")
    }


    fun refreshCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Обновляем все данные из API
                repository.refreshAllData()

                // Сбрасываем offset и загружаем первые 20
                currentOffset = 0
                loadCharacters(20, 0)

                // Обновляем общее количество
                totalCountInDb = repository.getTotalCharactersCount()

                // Обновляем состояние
                updateHasMoreDataState()

            } catch (e: Exception) {
                Timber.e(e, "Ошибка обновления данных")
                _error.value = "Ошибка обновления данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}