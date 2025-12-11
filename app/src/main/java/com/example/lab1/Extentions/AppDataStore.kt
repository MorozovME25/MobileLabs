package com.example.lab1.Extentions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class AppDataStore private constructor(context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")
    val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        @Volatile
        private var instance: AppDataStore? = null

        fun getInstance(context: Context): AppDataStore {
            return instance ?: synchronized(this) {
                instance ?: AppDataStore(context.applicationContext).also { instance = it }
            }
        }
    }
}