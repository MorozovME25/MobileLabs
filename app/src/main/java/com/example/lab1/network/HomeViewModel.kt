package com.example.lab1.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab1.Extentions.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.min

class HomeViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isInitialLoading = MutableStateFlow(true)
    private val _hasMoreData = MutableStateFlow(true)
    private val _lastLoadedId = MutableStateFlow(20) // Начинаем с 50 (первоначальная загрузка)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    // Реактивный поток ВСЕХ персонажей
    val characters: StateFlow<List<Character>> = repository.getAllCharactersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var maxApiLimit = 2000
    private var totalCountInDb = 0

    init {
        loadInitialData()
        monitorDataState()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isInitialLoading.value = true
            _isLoading.value = true
            _error.value = null

            try {
                // Проверяем, есть ли уже данные в базе
                val hasData = repository.hasDataInDatabase()
                Timber.d("Проверка данных в базе: $hasData")

                if (!hasData) {
                    Timber.d("Инициализация данных - загрузка первых 50 персонажей")
                    repository.initializeData()
                }

                // Получаем общее количество персонажей в базе
                totalCountInDb = repository.getTotalCharactersCount()
                _lastLoadedId.value = totalCountInDb
                Timber.d("Всего персонажей в базе после инициализации: $totalCountInDb")

            } catch (e: Exception) {
                Timber.e(e, "Ошибка инициализации данных")
                _error.value = "Ошибка инициализации данных: ${e.message}"
            } finally {
                _isInitialLoading.value = false
                _isLoading.value = false
            }
        }
    }

    private fun monitorDataState() {
        viewModelScope.launch {
            combine(
                repository.countAllCharactersFlow(),
                _lastLoadedId
            ) { totalCount, lastLoadedId ->
                totalCountInDb = totalCount
                updateHasMoreDataState(totalCount, lastLoadedId)
            }.collect()
        }
    }

    private fun updateHasMoreDataState(totalCount: Int, lastLoadedId: Int) {
        val hasMoreInApi = lastLoadedId < maxApiLimit
        val hasMoreData = hasMoreInApi

        _hasMoreData.value = hasMoreData
        Timber.d("Состояние данных: totalCount=$totalCount, lastLoadedId=$lastLoadedId, hasMoreInApi=$hasMoreInApi, hasMoreData=$hasMoreData")
    }

    /**
     * Загрузить следующую порцию персонажей из API (10 штук)
     */
    fun loadMoreCharacters() {
        if (_isLoading.value || !_hasMoreData.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val currentLastId = _lastLoadedId.value
                val nextStartId = currentLastId + 1
                val countToLoad = 10 // Загружаем по 10 персонажей за раз

                Timber.d("Загрузка дополнительных персонажей: startId=$nextStartId, count=$countToLoad")

                val newCharacters = repository.loadAdditionalCharacters(nextStartId, countToLoad)

                if (newCharacters.isNotEmpty()) {
                    // Обновляем последний загруженный ID
                    _lastLoadedId.value = newCharacters.maxByOrNull { it.id }?.id ?: currentLastId
                    Timber.d("Успешно загружено ${newCharacters.size} новых персонажей. Новый lastLoadedId: ${_lastLoadedId.value}")
                } else {
                    Timber.w("Не удалось загрузить новых персонажей")
                    _hasMoreData.value = false
                }

            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки дополнительных персонажей")
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Timber.d("Начало полного обновления данных")

                // Полная перезагрузка данных из API
                val refreshedCharacters = repository.refreshAllData()

                // Обновляем состояние
                totalCountInDb = refreshedCharacters.size
                _lastLoadedId.value = totalCountInDb
                _hasMoreData.value = totalCountInDb < maxApiLimit

                Timber.d("Данные успешно обновлены. Всего персонажей: $totalCountInDb")

            } catch (e: Exception) {
                Timber.e(e, "Ошибка обновления данных")
                _error.value = "Ошибка обновления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
