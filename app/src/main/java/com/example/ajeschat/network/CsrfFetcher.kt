package com.example.ajeschat.network

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

/**
 * Fetches CSRF token from a GET page (e.g. / or /chat). CodeIgniter uses csrf_test_name or meta tag.
 */
class CsrfFetcher(private val client: OkHttpClient, private val baseUrl: String) {

    fun fetchCsrfToken(): String? {
        val url = baseUrl.trimEnd('/') + "/chat"
        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return null
        return parseToken(body)
    }

    private fun parseToken(html: String): String? {
        // <input type="hidden" name="csrf_test_name" value="...">
        var p = Pattern.compile("name=[\"']csrf_test_name[\"']\\s+value=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        var m = p.matcher(html)
        if (m.find()) return m.group(1)?.trim()

        // <meta name="csrf-token" content="...">
        p = Pattern.compile("name=[\"']csrf-token[\"']\\s+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        m = p.matcher(html)
        if (m.find()) return m.group(1)?.trim()

        // name="csrf_token" value="..."
        p = Pattern.compile("name=[\"']csrf_token[\"']\\s+value=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
        m = p.matcher(html)
        if (m.find()) return m.group(1)?.trim()

        return null
    }
}
