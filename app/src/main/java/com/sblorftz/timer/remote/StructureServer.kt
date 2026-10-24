package com.sblorftz.timer.remote

import android.os.Handler
import android.os.Looper
import com.sblorftz.timer.Level
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.security.SecureRandom
import kotlin.concurrent.thread
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Livello come lo manda la pagina del telefono: il grande buio lo calcola la TV. */
@Serializable
data class RemoteLevel(val smallBlind: Int, val minutes: Int)

@Serializable
private data class RemoteState(val levels: List<RemoteLevel>, val current: Int?)

/**
 * Piccolo server HTTP sulla TV per modificare la struttura dal telefono, nella stessa wifi.
 *
 * Vive solo finché la schermata col QR è aperta. Ogni avvio genera un token nuovo che sta
 * nell'indirizzo del QR: senza token il server non risponde, quindi chi è in rete ma non
 * vede la TV non può toccare nulla.
 *
 * [onSave] viene chiamato sul thread principale con la struttura già validata.
 */
class StructureServer(private val onSave: (List<Level>) -> Unit) {
    /** Struttura e livello in corso da mostrare al telefono, aggiornati dalla UI. */
    @Volatile var levels: List<Level> = emptyList()
    @Volatile var currentIndex: Int? = null

    val token: String = ByteArray(6).also { SecureRandom().nextBytes(it) }
        .joinToString("") { "%02x".format(it) }

    private val json = Json { ignoreUnknownKeys = true }
    private val main = Handler(Looper.getMainLooper())
    private var socket: ServerSocket? = null

    /** Avvia il server e restituisce l'indirizzo da mettere nel QR, null se non c'è rete. */
    fun start(): String? {
        val ip = localIp() ?: return null
        val server = runCatching { ServerSocket(PORT) }.getOrElse { ServerSocket(0) }
        socket = server
        thread(name = "structure-server", isDaemon = true) {
            while (!server.isClosed) {
                val client = try {
                    server.accept()
                } catch (_: SocketException) {
                    break
                }
                thread(isDaemon = true) { client.use { handle(it) } }
            }
        }
        return "http://$ip:${server.localPort}/?t=$token"
    }

    fun stop() {
        runCatching { socket?.close() }
        socket = null
    }

    private fun handle(client: Socket) {
        client.soTimeout = 10_000
        val input = client.getInputStream()
        val out = client.getOutputStream()
        val request = runCatching { readRequest(input) }.getOrNull() ?: return
        val (method, target, body) = request
        val path = target.substringBefore('?')
        val query = target.substringAfter('?', "")
            .split('&')
            .associate { it.substringBefore('=') to it.substringAfter('=', "") }

        if (query["t"] != token) {
            if (path == "/") respond(out, 403, "text/html", StructurePage.expired)
            else respond(out, 403, "application/json", """{"error":"token"}""")
            return
        }

        when {
            method == "GET" && path == "/" ->
                respond(out, 200, "text/html", StructurePage.html)
            method == "GET" && path == "/api/structure" -> {
                val state = RemoteState(levels.map { RemoteLevel(it.smallBlind, it.minutes) }, currentIndex)
                respond(out, 200, "application/json", json.encodeToString(state))
            }
            method == "POST" && path == "/api/structure" -> {
                val parsed = runCatching { json.decodeFromString<List<RemoteLevel>>(body) }.getOrNull()
                val valid = parsed?.let(::toLevels)
                if (valid == null) {
                    respond(out, 400, "application/json", """{"error":"invalid"}""")
                } else {
                    main.post { onSave(valid) }
                    respond(out, 200, "application/json", """{"ok":true}""")
                }
            }
            else -> respond(out, 404, "text/plain", "not found")
        }
    }

    /** Stessi limiti del telecomando; un livello a 0 è il cambio fiches. */
    private fun toLevels(remote: List<RemoteLevel>): List<Level>? {
        if (remote.isEmpty() || remote.size > MAX_LEVELS) return null
        if (remote.none { it.smallBlind > 0 }) return null
        return remote.map {
            if (it.minutes !in 1..120) return null
            when {
                it.smallBlind == 0 -> Level.chipChange(it.minutes)
                it.smallBlind in 1..1_000_000 -> Level.blinds(it.smallBlind, it.minutes)
                else -> return null
            }
        }
    }

    private fun readRequest(input: InputStream): Triple<String, String, String>? {
        val head = ByteArrayOutputStream()
        // Header fino alla riga vuota, un byte alla volta: niente buffer che si mangia il body.
        while (!head.toByteArray().endsWith(CRLF2)) {
            val b = input.read()
            if (b < 0 || head.size() > MAX_HEAD) return null
            head.write(b)
        }
        val lines = head.toString(Charsets.UTF_8.name()).split("\r\n")
        val parts = lines.first().split(' ')
        if (parts.size < 2) return null
        val length = lines.drop(1)
            .firstOrNull { it.startsWith("content-length:", ignoreCase = true) }
            ?.substringAfter(':')?.trim()?.toIntOrNull() ?: 0
        if (length !in 0..MAX_BODY) return null
        val body = ByteArray(length)
        var read = 0
        while (read < length) {
            val n = input.read(body, read, length - read)
            if (n < 0) return null
            read += n
        }
        return Triple(parts[0], parts[1], String(body, Charsets.UTF_8))
    }

    private fun respond(out: OutputStream, status: Int, type: String, body: String) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        val reason = when (status) {
            200 -> "OK"; 400 -> "Bad Request"; 403 -> "Forbidden"; else -> "Not Found"
        }
        val head = "HTTP/1.1 $status $reason\r\n" +
            "Content-Type: $type; charset=utf-8\r\n" +
            "Content-Length: ${bytes.size}\r\n" +
            "Cache-Control: no-store\r\n" +
            "Connection: close\r\n\r\n"
        runCatching {
            out.write(head.toByteArray(Charsets.US_ASCII))
            out.write(bytes)
            out.flush()
        }
    }

    private fun ByteArray.endsWith(suffix: ByteArray): Boolean =
        size >= suffix.size && suffix.indices.all { this[size - suffix.size + it] == suffix[it] }

    companion object {
        private const val PORT = 8765
        private const val MAX_HEAD = 16 * 1024
        private const val MAX_BODY = 64 * 1024
        private const val MAX_LEVELS = 200
        private val CRLF2 = "\r\n\r\n".toByteArray()

        /** IPv4 della TV in rete locale (wifi o cavo). */
        fun localIp(): String? = runCatching {
            NetworkInterface.getNetworkInterfaces().toList()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }
                ?.hostAddress
        }.getOrNull()
    }
}
