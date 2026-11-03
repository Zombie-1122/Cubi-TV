package com.cubitv.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.cubitv.app.data.LibraryStore
import com.cubitv.app.source.SourceRegistry
import com.cubitv.app.source.StreamUrl
import kotlinx.coroutines.CancellationException

@Composable
fun PlayerScreen(sourceId: String, animeId: String, episodeId: String) {
    var stream by remember { mutableStateOf<StreamUrl?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sourceId, animeId, episodeId) {
        try {
            // First stream is used. Extend here to let the user pick quality.
            stream = SourceRegistry.get(sourceId).streams(animeId, episodeId).firstOrNull()
            if (stream == null) error = "No stream available"
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            error = e.message ?: "Failed to load stream"
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val s = stream
        when {
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            s == null -> CircularProgressIndicator()
            else -> VideoPlayer(s, sourceId, animeId, episodeId)
        }
    }
}

@Composable
private fun VideoPlayer(s: StreamUrl, sourceId: String, animeId: String, episodeId: String) {
    val ctx = LocalContext.current
    val player = remember(s) {
        val http = DefaultHttpDataSource.Factory().setDefaultRequestProperties(s.headers)
        ExoPlayer.Builder(ctx).setMediaSourceFactory(DefaultMediaSourceFactory(http)).build().apply {
            setMediaItem(MediaItem.fromUri(s.url))
            seekTo(LibraryStore.progress(sourceId, animeId, episodeId))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) {
        onDispose {
            LibraryStore.saveProgress(sourceId, animeId, episodeId, player.currentPosition, player.duration)
            player.release()
        }
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { c ->
            PlayerView(c).apply {
                this.player = player
                keepScreenOn = true
                isFocusable = true
                requestFocus() // so the TV remote D-pad controls playback
            }
        }
    )
}
