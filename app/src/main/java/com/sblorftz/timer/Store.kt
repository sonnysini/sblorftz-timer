package com.sblorftz.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
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

class Sounds(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // MEDIA: segue il volume che si regola col telecomando della TV.
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val oneMinute = pool.load(context, R.raw.one_minute, 1)
    private val tenSeconds = pool.load(context, R.raw.ten_seconds, 1)
    private val levelChange = pool.load(context, R.raw.level_change, 1)

    fun oneMinute() = play(oneMinute)
    fun tenSeconds() = play(tenSeconds)
    fun levelChange() = play(levelChange)

    private fun play(id: Int) {
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()
}
