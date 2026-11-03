package com.cubitv.app.source

data class Anime(
    val source: String,
    val id: String,
    val title: String,
    val cover: String? = null,
    val description: String = ""
)

data class Episode(val id: String, val number: Float, val title: String)

data class StreamUrl(
    val url: String,
    val quality: String = "auto",
    val headers: Map<String, String> = emptyMap()
)
