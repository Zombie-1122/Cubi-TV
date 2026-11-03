package com.cubitv.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cubitv.app.data.LibraryStore
import com.cubitv.app.source.Anime
import com.cubitv.app.source.SourceRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

private const val FAVORITES = "__favorites__"

@Composable
fun HomeScreen(isTv: Boolean, onOpen: (Anime) -> Unit) {
    UpdateChecker()
    var selected by remember { mutableStateOf(SourceRegistry.all.first().id) }
    var query by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selected, query) {
        loading = true; error = null
        try {
            if (selected == FAVORITES) {
                items = LibraryStore.favorites().filter { it.title.contains(query, true) }
            } else {
                if (query.isNotEmpty()) delay(400) // debounce typing
                items = SourceRegistry.get(selected).search(query)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.message ?: "Failed to load"
            items = emptyList()
        }
        loading = false
    }

    Column(Modifier.fillMaxSize().padding(if (isTv) 48.dp else 16.dp)) {
        Text("Cubi TV", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text("Search") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected == FAVORITES, { selected = FAVORITES }, label = { Text("★ Favorites") })
            SourceRegistry.all.forEach { s ->
                FilterChip(selected == s.id, { selected = s.id }, label = { Text(s.name) })
            }
        }
        Spacer(Modifier.height(12.dp))
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (isTv) 200.dp else 140.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items, key = { it.source + "|" + it.id }) { a -> PosterCard(a, isTv) { onOpen(a) } }
        }
    }
}

@Composable
fun PosterCard(a: Anime, isTv: Boolean, onClick: () -> Unit) {
    // TV uses the tv-material Card (focus highlight + scale for D-pad); phone uses the normal Card.
    if (isTv) androidx.tv.material3.Card(onClick = onClick) { Poster(a) }
    else Card(onClick = onClick) { Poster(a) }
}

@Composable
private fun Poster(a: Anime) {
    Column {
        Box(
            Modifier.fillMaxWidth().aspectRatio(2f / 3f).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (a.cover != null) AsyncImage(a.cover, a.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(a.title.take(1), style = MaterialTheme.typography.displayMedium)
        }
        Text(a.title, Modifier.padding(8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium)
    }
}
