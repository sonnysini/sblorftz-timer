package com.sblorftz.timer

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Struttura e torneo in corso, salvati in SharedPreferences come JSON. */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("sblorftz", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun loadStructure(): List<Level> =
        prefs.getString(KEY_STRUCTURE, null)
            ?.let { runCatching { json.decodeFromString<List<Level>>(it) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: DefaultStructure

    fun saveStructure(levels: List<Level>) {
        prefs.edit().putString(KEY_STRUCTURE, json.encodeToString(levels)).apply()
    }

    fun loadSession(): Session? =
        prefs.getString(KEY_SESSION, null)
            ?.let { runCatching { json.decodeFromString<Session>(it) }.getOrNull() }

    fun saveSession(session: Session) {
        prefs.edit().putString(KEY_SESSION, json.encodeToString(session)).apply()
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private companion object {
        const val KEY_STRUCTURE = "structure"
        const val KEY_SESSION = "session"
    }
}
