package com.example.ajeschat.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Persists cookies so session survives app restart. Uses same cookies on every request (backend session).
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val memory = ConcurrentHashMap<String, MutableList<Cookie>>()

    init {
        loadFromPrefs()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val key = url.host
        return memory[key]?.toList() ?: emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val key = url.host
        val existing = memory.getOrPut(key) { mutableListOf() }
        cookies.forEach { new ->
            existing.removeAll { it.name == new.name }
            if (!new.persistent || new.expiresAt > System.currentTimeMillis()) {
                existing.add(new)
            }
        }
        persist(key)
    }

    fun clear() {
        memory.clear()
        prefs.edit().clear().apply()
    }

    private fun loadFromPrefs() {
        val all = prefs.getStringSet(KEY_COOKIES, null) ?: return
        all.forEach { line ->
            val idx = line.indexOf('|')
            if (idx <= 0) return@forEach
            val key = line.substring(0, idx)
            val c = decodeCookie(line.substring(idx + 1)) ?: return@forEach
            memory.getOrPut(key) { mutableListOf() }.add(c)
        }
    }

    private fun persist(hostKey: String? = null) {
        val set = memory.flatMap { (key, list) ->
            list.map { encodeCookie(key, it) }
        }.toSet()
        prefs.edit().putStringSet(KEY_COOKIES, set).apply()
    }

    private fun encodeCookie(hostKey: String, c: Cookie): String {
        return "$hostKey|${c.name}|${c.value}|${c.domain}|${c.path}|${c.expiresAt}|${c.secure}|${c.httpOnly}|${c.persistent}"
    }

    private fun decodeCookie(s: String): Cookie? {
        val parts = s.split("|")
        if (parts.size < 8) return null
        var builder = Cookie.Builder()
            .name(parts[0])
            .value(parts[1])
            .domain(parts.getOrNull(2) ?: "")
            .path(parts.getOrNull(3) ?: "/")
            .expiresAt(parts.getOrNull(4)?.toLongOrNull() ?: 0L)
        if (parts.getOrNull(5)?.toBooleanStrictOrNull() == true) builder = builder.secure()
        if (parts.getOrNull(6)?.toBooleanStrictOrNull() == true) builder = builder.httpOnly()
        return builder.build()
    }

    companion object {
        private const val PREFS_NAME = "ajeschat_cookies"
        private const val KEY_COOKIES = "cookies"
    }
}
