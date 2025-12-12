package com.example.lab1.Extentions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Character(
    @Json(name = "name")
    val name: String,

    @Json(name = "culture")
    val culture: String?,

    @Json(name = "born")
    val born: String?,

    @Json(name = "titles")
    val titles: List<String>,

    @Json(name = "aliases")
    val aliases: List<String>,

    @Json(name = "playedBy")
    val playedBy: List<String>,

    @Json(name = "url")
    val url: String? = null,
    )
{
    // Вычисляемый ID из URL
    val id: Int
        get() = url?.let { url ->
            url.split("/").lastOrNull()?.toIntOrNull() ?: 0
        } ?: 0
}