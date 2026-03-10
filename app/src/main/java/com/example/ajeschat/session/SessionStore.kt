package com.example.ajeschat.session

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SessionStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ajeschat_session", Context.MODE_PRIVATE)

    fun save(s: Session) {
        prefs.edit().putString(KEY_JSON, """{"id":${s.id},"name":"${escape(s.name)}","role":"${escape(s.role)}","token":"${escape(s.token)}"}""").apply()
    }

    fun load(): Session? {
        val json = prefs.getString(KEY_JSON, null) ?: return null
        return try {
            val o = JSONObject(json)
            Session(
                o.optInt("id", -1),
                o.optString("name", ""),
                o.optString("role", ""),
                o.optString("token", "")
            )
        } catch (_: Exception) { null }
    }

    fun clear() {
        prefs.edit().remove(KEY_JSON).apply()
    }

    private fun escape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val KEY_JSON = "session"
    }
}
