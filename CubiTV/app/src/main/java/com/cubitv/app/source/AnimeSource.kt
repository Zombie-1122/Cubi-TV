package com.cubitv.app.source

/**
 * Implement this interface to add a new source. Then register it in SourceRegistry.
 * All functions are suspend and run off the main thread (see Http helper).
 * Only add sources you have the right to use.
 */
interface AnimeSource {
    val id: String          // unique, stable, e.g. "demo"
    val name: String        // shown in the UI

    /** Empty query = return popular / latest titles. */
    suspend fun search(query: String): List<Anime>
    suspend fun details(animeId: String): Anime
    suspend fun episodes(animeId: String): List<Episode>
    suspend fun streams(animeId: String, episodeId: String): List<StreamUrl>
}
