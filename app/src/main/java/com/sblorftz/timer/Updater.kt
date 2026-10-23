package com.sblorftz.timer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Aggiornamenti dall'app, come OpenTV: l'APK installato a mano non lo aggiorna nessuno.
 *
 * Legge l'ultima Release GitHub (la crea la CI a ogni tag `vX.Y.Z`, vedi
 * .github/workflows/release.yml) e la propone se il tag è più nuovo di [BuildConfig.VERSION_NAME].
 * Qualsiasi errore (niente rete, limite API, nessuna release) vale "nessun aggiornamento":
 * il controllo non deve mai dar fastidio a chi apre l'app per giocare.
 */
object Updater {
    const val REPO = "sonnysini/sblorftz-timer"

    data class Update(val versionName: String, val apkUrl: String, val notes: String)

    /** Una volta per avvio dell'app: "Più tardi" vale fino alla prossima apertura. */
    var checkedThisLaunch = false

    suspend fun check(): Update? = withContext(Dispatchers.IO) {
        runCatching {
            val release = JSONObject(get("https://api.github.com/repos/$REPO/releases/latest").use { it.readBytes().decodeToString() })
            if (release.optBoolean("draft") || release.optBoolean("prerelease")) return@runCatching null
            val tag = release.getString("tag_name")
            if (!isNewer(tag, BuildConfig.VERSION_NAME)) return@runCatching null
            val assets = release.getJSONArray("assets")
            val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk") } ?: return@runCatching null
            Update(
                versionName = tag.trimStart('v', 'V'),
                apkUrl = apk.getString("browser_download_url"),
                notes = cleanNotes(release.optString("body")),
            )
        }.getOrNull()
    }

    /** True se [tag] (es. `v2.0.1`) è una versione più alta di [current] (es. `2.0.0`). */
    fun isNewer(tag: String, current: String): Boolean {
        val a = parts(tag)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    private fun parts(v: String) = v.trim().trimStart('v', 'V').substringBefore('-')
        .split('.').map { p -> p.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

    /** Le note della release (sezione del CHANGELOG) senza Markdown né la riga finale della CI. */
    private fun cleanNotes(raw: String) = raw.substringBefore("\n---").lines()
        .map { it.trim().removePrefix("- ").removePrefix("* ").replace("**", "").replace("`", "") }
        .filter { it.isNotEmpty() }
        .joinToString(" · ")
        .take(300)

    /** Scarica l'APK e apre l'installer di sistema. [progress] riceve 0..1. */
    suspend fun downloadAndInstall(context: Context, update: Update, progress: (Float) -> Unit) {
        val apk = withContext(Dispatchers.IO) {
            // Sotto Android 7 l'installer vuole un file:// che possa leggere: niente cache privata.
            val dir = if (Build.VERSION.SDK_INT >= 24) File(context.cacheDir, "updates") else context.getExternalFilesDir(null)!!
            dir.mkdirs()
            val out = File(dir, "sblorftz-update.apk")
            val conn = URL(update.apkUrl).openConnection() as HttpURLConnection
            // GitHub risponde con un redirect verso il file vero.
            conn.instanceFollowRedirects = true
            try {
                conn.connectTimeout = 10_000
                conn.readTimeout = 20_000
                if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
                val total = conn.contentLength.toLong()
                conn.inputStream.use { input ->
                    out.outputStream().use { sink ->
                        val buffer = ByteArray(64 * 1024)
                        var written = 0L
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            sink.write(buffer, 0, n)
                            written += n
                            if (total > 0) progress(written.toFloat() / total)
                        }
                    }
                }
            } finally {
                conn.disconnect()
            }
            out
        }
        val uri: Uri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        } else {
            Uri.fromFile(apk)
        }
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }
        context.startActivity(intent)
    }

    private fun get(url: String) = (URL(url).openConnection() as HttpURLConnection).run {
        connectTimeout = 8_000
        readTimeout = 8_000
        useCaches = false
        setRequestProperty("Accept", "application/vnd.github+json")
        setRequestProperty("User-Agent", "SblorftzTimer")
        if (responseCode != 200) {
            disconnect()
            error("HTTP $responseCode")
        }
        inputStream
    }
}
