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
 * La repo è privata, quindi niente GitHub Releases: si legge un `latest.json` dalla
 * cartella pubblica del webdav (`tools/publish.sh` lo scrive insieme all'APK):
 *
 *     {"versionCode": 23, "versionName": "2.1.0", "apk": "sblorftz-timer-2.1.0.apk", "notes": "…"}
 *
 * Qualsiasi errore (niente rete, 404, json storto) vale "nessun aggiornamento": il
 * controllo non deve mai dar fastidio a chi apre l'app per giocare.
 */
object Updater {
    const val BASE_URL = "https://files.sonnysini.it/Pubblico/sblorftz/"

    data class Update(val versionCode: Int, val versionName: String, val apkUrl: String, val notes: String)

    /** Una volta per avvio dell'app: "Più tardi" vale fino alla prossima apertura. */
    var checkedThisLaunch = false

    suspend fun check(): Update? = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject(get(BASE_URL + "latest.json").use { it.readBytes().decodeToString() })
            val code = json.getInt("versionCode")
            if (code <= BuildConfig.VERSION_CODE) return@runCatching null
            Update(
                versionCode = code,
                versionName = json.getString("versionName"),
                apkUrl = BASE_URL + json.getString("apk"),
                notes = json.optString("notes"),
            )
        }.getOrNull()
    }

    /** Scarica l'APK e apre l'installer di sistema. [progress] riceve 0..1. */
    suspend fun downloadAndInstall(context: Context, update: Update, progress: (Float) -> Unit) {
        val apk = withContext(Dispatchers.IO) {
            // Sotto Android 7 l'installer vuole un file:// che possa leggere: niente cache privata.
            val dir = if (Build.VERSION.SDK_INT >= 24) File(context.cacheDir, "updates") else context.getExternalFilesDir(null)!!
            dir.mkdirs()
            val out = File(dir, "sblorftz-update.apk")
            val conn = URL(update.apkUrl).openConnection() as HttpURLConnection
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
        if (responseCode != 200) {
            disconnect()
            error("HTTP $responseCode")
        }
        inputStream
    }
}
