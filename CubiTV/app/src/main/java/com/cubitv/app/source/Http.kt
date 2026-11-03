package com.cubitv.app.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object Http {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String =
        withContext(Dispatchers.IO) {
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 15000
            c.readTimeout = 15000
            headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
            try {
                c.inputStream.bufferedReader().use { it.readText() }
            } finally {
                c.disconnect()
            }
        }
}
