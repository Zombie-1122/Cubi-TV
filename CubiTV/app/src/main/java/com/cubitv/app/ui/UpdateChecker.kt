package com.cubitv.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.cubitv.app.data.Updater
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Put once on the home screen. Checks GitHub on launch and offers the update. */
@Composable
fun UpdateChecker() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var release by remember { mutableStateOf<Updater.Release?>(null) }
    var dismissed by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try { release = Updater.check() } catch (e: Exception) { if (e is CancellationException) throw e }
    }

    val r = release
    if (r != null && !dismissed) {
        AlertDialog(
            onDismissRequest = { if (progress == null) dismissed = true },
            title = { Text("Update available: v${r.version}") },
            text = {
                Column {
                    Text(r.notes.take(300).ifBlank { "A new version is ready." })
                    progress?.let { LinearProgressIndicator(progress = { it / 100f }) }
                    message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(enabled = progress == null, onClick = {
                    if (!Updater.canInstall(ctx)) {
                        message = "Allow installs for Cubi TV, come back, then tap Update again."
                        Updater.openInstallPermission(ctx)
                        return@Button
                    }
                    message = null; progress = 0
                    scope.launch {
                        try {
                            val f = Updater.download(ctx, r.apkUrl) { progress = it }
                            Updater.install(ctx, f)
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            message = "Download failed: ${e.message}"
                        }
                        progress = null
                    }
                }) { Text("Update now") }
            },
            dismissButton = {
                TextButton(enabled = progress == null, onClick = { dismissed = true }) { Text("Later") }
            }
        )
    }
}
