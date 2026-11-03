package com.cubitv.app.source

/** Add new sources to this list. That's the only place you need to touch. */
object SourceRegistry {
    val all: List<AnimeSource> = listOf(
        DemoSource(),
        // JSON catalog you host or are licensed to use (see README for the format):
        // GenericJsonSource("mycatalog", "My Catalog", "https://example.com/catalog.json"),
    )

    fun get(id: String): AnimeSource = all.first { it.id == id }
}
