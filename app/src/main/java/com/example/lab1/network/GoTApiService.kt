package com.example.lab1.network

import com.example.lab1.Extentions.Character
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GoTApiService {
    @GET("characters/{id}")
    suspend fun getCharacter(@Path("id") id: Int): Character

    companion object {
        private const val BASE_URL = "https://www.anapioficeandfire.com/api/"

        fun create(): GoTApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GoTApiService::class.java)
        }
    }
}