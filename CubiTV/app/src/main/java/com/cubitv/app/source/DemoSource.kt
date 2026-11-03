package com.cubitv.app.source

/** Built-in demo using the open-licensed Blender Foundation films, so the app works out of the box. */
class DemoSource : AnimeSource {
    override val id = "demo"
    override val name = "Demo (open films)"

    private val base = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/"
    private val titles = listOf(
        Triple("bbb", "Big Buck Bunny", "BigBuckBunny.mp4"),
        Triple("sintel", "Sintel", "Sintel.mp4"),
        Triple("ed", "Elephants Dream", "ElephantsDream.mp4"),
        Triple("tos", "Tears of Steel", "TearsOfSteel.mp4"),
    )

    private fun toAnime(t: Triple<String, String, String>) =
        Anime(id, t.first, t.second, null, "Open-licensed short film by the Blender Foundation.")

    override suspend fun search(query: String) =
        titles.filter { it.second.contains(query, ignoreCase = true) }.map(::toAnime)

    override suspend fun details(animeId: String) = toAnime(titles.first { it.first == animeId })

    override suspend fun episodes(animeId: String) =
        listOf(Episode("1", 1f, titles.first { it.first == animeId }.second))

    override suspend fun streams(animeId: String, episodeId: String) =
        listOf(StreamUrl(base + titles.first { it.first == animeId }.third, "720p"))
}
