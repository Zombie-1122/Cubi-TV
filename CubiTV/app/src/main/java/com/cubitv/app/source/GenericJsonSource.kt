package com.cubitv.app.source

import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads a catalog from a JSON URL, so you can add a catalog without writing code.
 * Format:
 * { "anime": [ { "id":"1", "title":"Name", "cover":"https://...jpg", "description":"...",
 *     "episodes": [ { "id":"1", "number":1, "title":"Ep 1",
 *        "streams": [ { "url":"https://.../index.m3u8", "quality":"1080p" } ] } ] } ] }
 */
class GenericJsonSource(
    override val id: String,
    override val name: String,
    private val catalogUrl: String
) : AnimeSource {

    private var cache: JSONArray? = null

    private suspend fun catalog(): JSONArray =
        cache ?: JSONObject(Http.get(catalogUrl)).getJSONArray("anime").also { cache = it }

    private suspend fun find(animeId: String): JSONObject {
        val a = catalog()
        for (i in 0 until a.length()) if (a.getJSONObject(i).getString("id") == animeId) return a.getJSONObject(i)
        error("Not found: $animeId")
    }

    private fun JSONObject.toAnime() = Anime(
        id, getString("id"), getString("title"),
        optString("cover").ifEmpty { null }, optString("description")
    )

    override suspend fun search(query: String): List<Anime> {
        val a = catalog()
        return (0 until a.length()).map { a.getJSONObject(it) }
            .filter { it.getString("title").contains(query, ignoreCase = true) }
            .map { it.toAnime() }
    }

    override suspend fun details(animeId: String) = find(animeId).toAnime()

    override suspend fun episodes(animeId: String): List<Episode> {
        val e = find(animeId).optJSONArray("episodes") ?: return emptyList()
        return (0 until e.length()).map {
            val o = e.getJSONObject(it)
            Episode(o.getString("id"), o.optDouble("number", (it + 1).toDouble()).toFloat(),
                o.optString("title", "Episode ${it + 1}"))
        }
    }

    override suspend fun streams(animeId: String, episodeId: String): List<StreamUrl> {
        val e = find(animeId).getJSONArray("episodes")
        for (i in 0 until e.length()) {
            val o = e.getJSONObject(i)
            if (o.getString("id") == episodeId) {
                val s = o.getJSONArray("streams")
                return (0 until s.length()).map {
                    val x = s.getJSONObject(it)
                    StreamUrl(x.getString("url"), x.optString("quality", "auto"))
                }
            }
        }
        return emptyList()
    }
}
