package com.cubitv.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.cubitv.app.BuildConfig
import com.cubitv.app.source.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Checks the latest GitHub Release of BuildConfig.GITHUB_REPO and installs its .apk. */
object Updater {
    data class Release(val version: String, val notes: String, val apkUrl: String)

    suspend fun check(): Release? {
        if (BuildConfig.GITHUB_REPO.startsWith("YOUR_USER")) return null // repo not configured
        val json = JSONObject(
            Http.get(
                "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest",
                mapOf("Accept" to "application/vnd.github+json", "User-Agent" to "CubiTV-Updater")
            )
        )
        val version = json.getString("tag_name").removePrefix("v")
        if (!isNewer(version, BuildConfig.VERSION_NAME)) return null
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val a = assets.getJSONObject(i)
            if (a.getString("name").endsWith(".apk"))
                return Release(version, json.optString("body"), a.getString("browser_download_url"))
        }
        return null
    }

    private fun isNewer(remote: String, local: String): Boolean {
        fun parts(v: String) = v.substringBefore("-").split(".").map { it.toIntOrNull() ?: 0 }
        val r = parts(remote); val l = parts(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }; val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    fun canInstall(ctx: Context) =
        Build.VERSION.SDK_INT < 26 || ctx.packageManager.canRequestPackageInstalls()

    /** Opens the system page where the user allows this app to install updates. */
    fun openInstallPermission(ctx: Context) {
        ctx.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    suspend fun download(ctx: Context, url: String, onProgress: (Int) -> Unit): File =
        withContext(Dispatchers.IO) {
            val dir = File(ctx.cacheDir, "updates").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
            val out = File(dir, "update.apk")
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 15000; c.readTimeout = 30000
            try {
                val total = c.contentLengthLong
                c.inputStream.use { input ->
                    out.outputStream().use { o ->
                        val buf = ByteArray(64 * 1024); var done = 0L; var n: Int
                        while (input.read(buf).also { n = it } >= 0) {
                            o.write(buf, 0, n); done += n
                            if (total > 0) onProgress((done * 100 / total).toInt())
                        }
                    }
                }
            } finally { c.disconnect() }
            out
        }

    fun install(ctx: Context, apk: File) {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
