package ru.hyper.messenger.data.remote

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

class SocketIoClient(private val baseUrl: String, private val cookie: String) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handlers = ConcurrentHashMap<String, MutableList<(JSONArray) -> Unit>>()
    private val writeLock = Mutex()
    val connected = MutableStateFlow(false)

    @Volatile private var running = false
    @Volatile private var sid: String? = null
    @Volatile private var opened = false
    private var pingMs = 25_000L
    private var job: Job? = null

    var onOpen: (() -> Unit)? = null
    var onClosed: (() -> Unit)? = null

    fun on(event: String, handler: (JSONArray) -> Unit) {
        handlers.getOrPut(event) { mutableListOf() }.add(handler)
    }

    fun connect() {
        if (job?.isActive == true) return
        running = true
        job = scope.launch { loop() }
    }

    fun disconnect() {
        running = false
        job?.cancel()
        job = null
        sid = null
        opened = false
        connected.value = false
        onClosed?.invoke()
    }

    fun emit(event: String, payload: Any? = null) {
        scope.launch { runCatching { send(event, payload) } }
    }

    private suspend fun loop() {
        var attempt = 0
        while (running) {
            val result = runCatching {
                openTransport()
                attempt = 0
                pollTransport()
            }
            connected.value = false
            if (!running) break
            onClosed?.invoke()
            result.exceptionOrNull()
            attempt++
            delay(min(20_000L, 1_200L * attempt))
        }
        connected.value = false
    }

    private suspend fun openTransport() {
        val handshake = httpGet("$baseUrl/socket.io/?EIO=4&transport=polling&t=${System.currentTimeMillis()}")
        if (!handshake.startsWith("0")) error("handshake failed")
        val json = JSONObject(handshake.substring(1))
        sid = json.getString("sid")
        pingMs = json.optLong("pingInterval", 25_000L)
        opened = false
        postPacket("40")
    }

    private suspend fun pollTransport() {
        while (running && sid != null) {
            val body = httpGet(pollUrl())
            body.split('\u001e').forEach { if (it.isNotEmpty()) handlePacket(it) }
        }
    }

    private suspend fun handlePacket(packet: String) {
        when {
            packet.startsWith("2") -> postPacket("3")
            packet.startsWith("3") -> Unit
            packet == "40" || packet.startsWith("40{") -> {
                if (!opened) {
                    opened = true
                    connected.value = true
                    onOpen?.invoke()
                }
            }
            packet.startsWith("42") -> dispatch(packet.substring(2))
            packet.startsWith("41") || packet.startsWith("44") -> running = false
            else -> Unit
        }
    }

    private fun dispatch(payload: String) {
        val array = runCatching { JSONArray(payload) }.getOrNull() ?: return
        val event = array.optString(0)
        if (event.isBlank()) return
        val args = JSONArray()
        for (index in 1 until array.length()) args.put(array.get(index))
        handlers[event]?.forEach { handler -> runCatching { handler(args) } }
    }

    private suspend fun send(event: String, payload: Any?) {
        val array = JSONArray().put(event)
        if (payload != null) array.put(payload)
        postPacket("42$array")
    }

    private suspend fun postPacket(packet: String): String = writeLock.withLock {
        val connection = (URL(pollUrl()).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 25_000
            doOutput = true
            setRequestProperty("Cookie", cookie)
            setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
            setRequestProperty("Accept-Encoding", "identity")
        }
        val result = runCatching {
            connection.outputStream.use { it.write(packet.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) error("post $code")
            connection.inputStream.bufferedReader().use { it.readText() }
        }
        connection.disconnect()
        result.getOrDefault("")
    }

    private fun httpGet(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = (pingMs + 15_000L).toInt()
            setRequestProperty("Cookie", cookie)
            setRequestProperty("Accept-Encoding", "identity")
            setRequestProperty("User-Agent", "HYPER-Android/1.1")
        }
        return try {
            val code = connection.responseCode
            if (code !in 200..299) error("get $code")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun pollUrl(): String =
        "$baseUrl/socket.io/?EIO=4&transport=polling&sid=${sid.orEmpty()}"
}
