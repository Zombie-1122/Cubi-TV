package com.cubitv.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.cubitv.app.data.LibraryStore
import com.cubitv.app.source.Anime
import com.cubitv.app.source.Episode
import com.cubitv.app.source.SourceRegistry
import kotlinx.coroutines.CancellationException

@Composable
fun DetailScreen(sourceId: String, animeId: String, isTv: Boolean, onPlay: (String) -> Unit) {
    var anime by remember { mutableStateOf<Anime?>(null) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var fav by remember { mutableStateOf(false) }

    LaunchedEffect(sourceId, animeId) {
        try {
            val src = SourceRegistry.get(sourceId)
            val a = src.details(animeId)
            anime = a
            fav = LibraryStore.isFavorite(a)
            episodes = src.episodes(animeId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.message ?: "Failed to load"
        }
    }

    Column(Modifier.fillMaxSize().padding(if (isTv) 48.dp else 16.dp)) {
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        anime?.let { a ->
            Text(a.title, style = MaterialTheme.typography.headlineMedium)
            if (a.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(a.description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { LibraryStore.toggleFavorite(a); fav = !fav }) {
                Text(if (fav) "★ In favorites" else "☆ Add to favorites")
            }
            Spacer(Modifier.height(12.dp))
            Text("Episodes", style = MaterialTheme.typography.titleMedium)
        } ?: if (error == null) LinearProgressIndicator(Modifier.fillMaxWidth()) else Unit

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(episodes, key = { it.id }) { ep ->
                var focused by remember { mutableStateOf(false) }
                val resumed = LibraryStore.progress(sourceId, animeId, ep.id) > 0
                Row(
                    Modifier.fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused }
                        .border(2.dp, if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp))
                        .clickable { onPlay(ep.id) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${ep.number.toInt()}. ${ep.title}")
                    if (resumed) Text("resume", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
