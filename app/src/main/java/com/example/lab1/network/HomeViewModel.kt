package com.example.lab1.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab1.Extentions.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val loadedCharacters = repository.getCharactersInRange(801, 850)
                _characters.value = loadedCharacters
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLoad() {
        loadCharacters()
    }

    fun getCharactersForBackup(): List<Character> {
        return _characters.value
    }

    companion object {
        const val START_ID = 801
        const val END_ID = 850
    }
}