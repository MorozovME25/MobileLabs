package com.example.lab1.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(CharacterRepository.getInstance()) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(dataStore) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

//class ViewModelFactory : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return HomeViewModel(CharacterRepository.getInstance()) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}