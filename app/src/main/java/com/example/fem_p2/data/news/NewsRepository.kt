package com.example.fem_p2.data.news

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class NewsRepository(private val client: OkHttpClient) {
    suspend fun fetchMadridHeadlines(limit: Int = 2): Result<List<String>> = runCatching {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(NEWS_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("No se pudieron cargar las noticias (${response.code})")
                }

                val body = response.body?.string()
                    ?: throw IllegalStateException("Respuesta vacía de noticias")

                val document = Jsoup.parse(body)
                val headlines = mutableListOf<String>()
                val selectors = listOf(
                    "div.noticiasDestacadas h3 a",
                    "div.destacado__contenedor h3 a",
                    "article h3 a",
                    "h3 a",
                    "h2 a"
                )

                for (selector in selectors) {
                    val elements = document.select(selector)
                    for (element in elements) {
                        val text = element.text().trim()
                        if (text.isNotEmpty() && !headlines.contains(text)) {
                            headlines.add(text)
                            if (headlines.size >= limit) {
                                break
                            }
                        }
                    }
                    if (headlines.size >= limit) {
                        break
                    }
                }

                if (headlines.isEmpty()) {
                    val fallback = document.select("h3, h2")
                        .map { it.text().trim() }
                        .filter { it.isNotEmpty() }
                    headlines.addAll(fallback)
                }

                val deduped = headlines.distinct().take(limit)
                if (deduped.isEmpty()) {
                    throw IllegalStateException("No se encontraron titulares de noticias")
                }

                deduped
            }
        }
    }

    companion object {
        private const val NEWS_URL = "https://www.madrid.es/portal/site/munimadrid"
    }
}