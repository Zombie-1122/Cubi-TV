package com.cubitv.app.data

import android.content.Context
import android.content.SharedPreferences
import com.cubitv.app.source.Anime
import org.json.JSONArray
import org.json.JSONObject

/** Favorites + resume positions, stored in SharedPreferences. */
object LibraryStore {
    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences("library", Context.MODE_PRIVATE)
    }

    fun favorites(): List<Anime> {
        val arr = JSONArray(prefs.getString("favs", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Anime(o.getString("source"), o.getString("id"), o.getString("title"),
                o.optString("cover").ifEmpty { null }, o.optString("description"))
        }
    }

    fun isFavorite(a: Anime) = favorites().any { it.source == a.source && it.id == a.id }

    fun toggleFavorite(a: Anime) {
        val list = favorites().toMutableList()
        if (!list.removeAll { it.source == a.source && it.id == a.id }) list.add(a)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("source", it.source).put("id", it.id).put("title", it.title)
                .put("cover", it.cover ?: "").put("description", it.description))
        }
        prefs.edit().putString("favs", arr.toString()).apply()
    }

    private fun key(s: String, a: String, e: String) = "p|$s|$a|$e"

    fun progress(s: String, a: String, e: String) = prefs.getLong(key(s, a, e), 0L)

    fun saveProgress(s: String, a: String, e: String, pos: Long, duration: Long) {
        val done = duration > 0 && pos > duration - 10_000
        prefs.edit().apply { if (done || pos < 5_000) remove(key(s, a, e)) else putLong(key(s, a, e), pos) }.apply()
    }
}
