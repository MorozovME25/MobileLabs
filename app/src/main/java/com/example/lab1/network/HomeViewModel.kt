package com.example.lab1.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab1.Extentions.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isInitialLoading = MutableStateFlow(true)
    private val _hasMoreData = MutableStateFlow(true)

    // Сколько персонажей показывать пользователю (пагинация на уровне UI)
    private val _displayedCount = MutableStateFlow(20) // Начинаем с 20

    // Последний ID, загруженный из API (для продолжения загрузки)
    private val _lastApiId = MutableStateFlow(50) // После первоначальной загрузки 50

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private var totalInDatabase = 0 // Общее количество в базе
    private var maxApiLimit = 2000 // Максимум от API

    // Комбинированный реактивный поток: все персонажи из базы + логика отображения
    val characters: StateFlow<List<Character>> = combine(
        repository.getAllCharactersFlow(), // Все данные из базы (реактивно!)
        _displayedCount // Сколько показывать
    ) { allCharacters, countToDisplay ->
        // Берем только нужное количество для отображения
        allCharacters.take(countToDisplay)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        setupDataMonitoring()
        loadInitialData()
    }

    private fun setupDataMonitoring() {
        viewModelScope.launch {
            repository.getTotalCountFlow().collect { totalCount ->
                totalInDatabase = totalCount
                Timber.d("Всего персонажей в базе: $totalCount")
                updateHasMoreDataState()
            }
        }
    }

    private fun updateHasMoreDataState() {
        val canLoadMoreFromDb = _displayedCount.value < totalInDatabase
        val canLoadMoreFromApi = _lastApiId.value < maxApiLimit

        _hasMoreData.value = canLoadMoreFromDb || canLoadMoreFromApi
        Timber.d("Состояние загрузки: displayed=${_displayedCount.value}, totalDb=$totalInDatabase, lastApiId=${_lastApiId.value}, hasMore=${_hasMoreData.value}")
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isInitialLoading.value = true
            _isLoading.value = true
            _error.value = null

            try {
                val hasData = repository.hasDataInDatabase()
                Timber.d("Проверка данных в базе: $hasData")

                if (!hasData) {
                    // Загружаем первоначальные 50 персонажей
                    Timber.d("Загрузка первоначальных 50 персонажей")
                    repository.initializeData()
                    _lastApiId.value = 50 // После загрузки 50 персонажей
                }

                // Показываем первые 20
                _displayedCount.value = 20

            } catch (e: Exception) {
                Timber.e(e, "Ошибка инициализации данных")
                _error.value = "Ошибка инициализации: ${e.message}"
            } finally {
                _isInitialLoading.value = false
                _isLoading.value = false
            }
        }
    }

    /**
     * Загрузить следующую порцию данных (10 персонажей)
     * Логика:
     * 1. Если есть еще данные в базе - показываем следующие 10
     * 2. Если данные в базе закончились - загружаем 10 из API
     */
    fun loadMoreCharacters() {
        if (_isLoading.value || !_hasMoreData.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val currentDisplayed = _displayedCount.value
                val currentTotalInDb = totalInDatabase

                Timber.d("Загрузка следующей порции. Текущие данные: displayed=$currentDisplayed, totalInDb=$currentTotalInDb")

                if (currentDisplayed < currentTotalInDb) {
                    // Есть еще данные в базе для отображения
                    val newCount = minOf(currentDisplayed + 10, currentTotalInDb)
                    _displayedCount.value = newCount
                    Timber.d("Показано еще ${newCount - currentDisplayed} персонажей из базы. Всего отображается: $newCount")
                } else {
                    // Данные в базе закончились, грузим из API
                    loadFromApi()
                }

                updateHasMoreDataState()

            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки дополнительных данных")
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadFromApi() {
        val startId = _lastApiId.value + 1
        val countToLoad = 10
        val endId = startId + countToLoad - 1

        if (startId > maxApiLimit) {
            Timber.w("Достигнут лимит API ($maxApiLimit)")
            _hasMoreData.value = false
            return
        }

        Timber.d("Загрузка из API: персонажи $startId-$endId")
        val newCharacters = repository.loadCharactersFromApi(startId, countToLoad)

        if (newCharacters.isNotEmpty()) {
            // Обновляем последний ID
            _lastApiId.value = newCharacters.maxByOrNull { it.id }?.id ?: _lastApiId.value

            // Увеличиваем количество отображаемых персонажей
            _displayedCount.value += newCharacters.size

            Timber.d("Загружено из API: ${newCharacters.size} персонажей. Новый lastApiId: ${_lastApiId.value}, displayed: ${_displayedCount.value}")
        } else {
            Timber.w("API вернул пустой результат. Больше данных нет.")
            _hasMoreData.value = false
        }
    }

    fun refreshCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Полная перезагрузка данных
                repository.refreshAllData()

                // Сбрасываем состояние
                _displayedCount.value = 20
                _lastApiId.value = 50 // После refresh загружается 50 персонажей

                Timber.d("Данные успешно обновлены. Показываем первые 20 персонажей.")

            } catch (e: Exception) {
                Timber.e(e, "Ошибка обновления данных")
                _error.value = "Ошибка обновления: ${e.message}"
            } finally {
                _isLoading.value = false
                updateHasMoreDataState()
            }
        }
    }
}
