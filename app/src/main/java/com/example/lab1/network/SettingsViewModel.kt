package com.example.lab1.network

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab1.Extentions.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _userName = MutableStateFlow("Пользователь")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isDarkThemeEnabled = MutableStateFlow(false)
    val isDarkThemeEnabled: StateFlow<Boolean> = _isDarkThemeEnabled.asStateFlow()

    private val _showNotifications = MutableStateFlow(true)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _fontSize = MutableStateFlow(16f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _backupFileName = MutableStateFlow("got_characters_backup_1.txt")
    val backupFileName: StateFlow<String> = _backupFileName.asStateFlow()

    private val _backupStatus = MutableStateFlow("Нет резервной копии")
    val backupStatus: StateFlow<String> = _backupStatus.asStateFlow()

    private val _restoreStatus = MutableStateFlow("Нет резервной копии для восстановления")
    val restoreStatus: StateFlow<String> = _restoreStatus.asStateFlow()

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    // Состояние хранения файла
    private val _isFileInExternalStorage = MutableStateFlow(false)
    val isFileInExternalStorage: StateFlow<Boolean> = _isFileInExternalStorage.asStateFlow()

    private val _isFileInInternalStorage = MutableStateFlow(false)
    val isFileInInternalStorage: StateFlow<Boolean> = _isFileInInternalStorage.asStateFlow()

    // Номер по списку
    val studentNumber = 17

    // Ключи для DataStore
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    private val BACKUP_FILE_NAME_KEY = stringPreferencesKey("backup_file_name")

    init {
        loadSettingsFromDataStore()
    }

    private fun loadSettingsFromDataStore() {
        viewModelScope.launch {
            val preferences = dataStore.data.first()
            _userName.value = preferences[USER_NAME_KEY] ?: "Пользователь"
            _isDarkThemeEnabled.value = preferences[DARK_THEME_KEY] ?: false
            _backupFileName.value = preferences[BACKUP_FILE_NAME_KEY] ?: "got_characters_backup_17.txt"
        }
    }

    fun setUserName(name: String) {
        _userName.value = name
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[USER_NAME_KEY] = name
            }
        }
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        _isDarkThemeEnabled.value = enabled
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_THEME_KEY] = enabled
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _showNotifications.value = enabled
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size
    }

    fun setBackupFileName(name: String) {
        _backupFileName.value = name
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[BACKUP_FILE_NAME_KEY] = name
            }
        }
    }

    fun updateCharacters(newCharacters: List<Character>) {
        _characters.value = newCharacters
    }

    fun createBackupFile(context: Context, characters: List<Character>, fileName: String): Boolean {
        return try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

            val backupFile = File(documentsDir, fileName)
            backupFile.parentFile?.mkdirs()

            println("[createBackupFile] backupFile = $backupFile")

            // Если файл уже существует во внешнем хранилище, удаляем его
            if (backupFile.exists()) {
                backupFile.delete()
            }

            // Если файл существует во внутреннем хранилище, удаляем его оттуда
            val internalFile = File(context.filesDir, fileName)
            if (internalFile.exists()) {
                internalFile.delete()
            }

            FileOutputStream(backupFile).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write("Резервная копия персонажей Игры Престолов\n")
                    writer.write("Создано: ${Date()}\n")
                    writer.write("Количество персонажей: ${characters.size}\n\n")

                    characters.forEach { character ->
                        writer.write("Имя: ${character.name}\n")
                        writer.write("Культура: ${character.culture ?: "Неизвестно"}\n")
                        writer.write("Родился: ${character.born ?: "Неизвестно"}\n")
                        writer.write("Титулы: ${character.titles.joinToString(", ") { it.ifEmpty { "Отсутствуют" } }}\n")
                        writer.write("Псевдонимы: ${character.aliases.joinToString(", ") { it.ifEmpty { "Отсутствуют" } }}\n")
                        writer.write("Играет: ${character.playedBy.joinToString(", ") { it.ifEmpty { "Не снимался в сериале" } }}\n")
                        writer.write("-".repeat(50) + "\n\n")
                    }
                }
            }

            // Обновляем состояние
            _isFileInExternalStorage.value = true
            _isFileInInternalStorage.value = false

            true
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error creating backup file: ${e.message}", e)
            false
        }
    }

    // УДАЛЕНИЕ файла из внешнего хранилища и ПЕРЕМЕЩЕНИЕ во внутреннее
    fun moveBackupToInternalStorage(context: Context, fileName: String): Boolean {
        return try {
            val externalFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val internalFile = File(context.filesDir, fileName)

            if (!externalFile.exists()) {
                Log.w("SettingsViewModel", "External backup file not found")
                return false
            }

            // Копируем файл из внешнего во внутреннее хранилище
            copyFile(externalFile, internalFile)

            // Удаляем файл из внешнего хранилища
            externalFile.delete()

            // Обновляем состояние
            _isFileInExternalStorage.value = false
            _isFileInInternalStorage.value = true

            true
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error moving backup to internal storage: ${e.message}", e)
            false
        }
    }

    // ВОССТАНОВЛЕНИЕ файла из внутреннего хранилища во внешнее
    fun restoreBackupFromInternalStorage(context: Context, fileName: String): Boolean {
        return try {
            val internalFile = File(context.filesDir, fileName)
            val externalFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            if (!internalFile.exists()) {
                Log.w("SettingsViewModel", "Internal backup file not found")
                return false
            }

            externalFile.parentFile?.mkdirs()
            println("[restoreBackupFromInternalStorage] externalFile = $externalFile")
            println("[restoreBackupFromInternalStorage] internalFile = $internalFile")

            copyFile(internalFile, externalFile)

            // Удаляем файл из внутреннего хранилища
            internalFile.delete()

            // Обновляем состояние
            _isFileInExternalStorage.value = true
            _isFileInInternalStorage.value = false

            true
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error restoring backup: ${e.message}", e)
            false
        }
    }

    // Вспомогательный метод для копирования файлов
    private fun copyFile(source: File, destination: File) {
        source.inputStream().use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    suspend fun restoreFromExternalFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File(context.cacheDir, "temp_restore.txt")
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                // Здесь можно добавить логику парсинга файла и восстановления данных
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error restoring from external file: ${e.message}", e)
            false
        }
    }

    fun updateBackupStatus(backupFile: File, internalBackupFile: File) {
        viewModelScope.launch {
            _isFileInExternalStorage.value = backupFile.exists()
            _isFileInInternalStorage.value = internalBackupFile.exists()

            if (backupFile.exists()) {
                val fileSize = formatFileSize(backupFile.length())
                val lastModified = formatLastModified(backupFile.lastModified())
                _backupStatus.value = "Файл: ${backupFile.name}\nРазмер: $fileSize\nСоздан: $lastModified"
            } else {
                _backupStatus.value = "Нет резервной копии во внешнем хранилище"
            }

            if (internalBackupFile.exists()) {
                val fileSize = formatFileSize(internalBackupFile.length())
                val lastModified = formatLastModified(internalBackupFile.lastModified())
                _restoreStatus.value = "Файл: ${internalBackupFile.name}\nРазмер: $fileSize\nСоздан: $lastModified"
            } else {
                _restoreStatus.value = "Нет резервной копии для восстановления"
            }
        }
    }

    fun getCurrentBackupFileName(): String {
        return _backupFileName.value
    }

    fun backupFileExists(context: Context, fileName: String): Boolean {
        val backupFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        return backupFile.exists()
    }

    fun internalBackupFileExists(context: Context, fileName: String): Boolean {
        val internalFile = File(context.filesDir, fileName)
        return internalFile.exists()
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    private fun formatLastModified(timestamp: Long): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(timestamp))
    }
}