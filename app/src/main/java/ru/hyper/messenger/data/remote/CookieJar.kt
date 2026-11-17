package ru.hyper.messenger.data.remote

import java.net.HttpURLConnection

class CookieJar {
    private val cookies = LinkedHashMap<String, String>()

    fun header(): String = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }

    fun store(connection: HttpURLConnection) {
        connection.headerFields.forEach { (key, values) ->
            if (!key.equals("Set-Cookie", ignoreCase = true)) return@forEach
            values.orEmpty().forEach { parse(it) }
        }
    }

    private fun parse(raw: String) {
        val pair = raw.substringBefore(';')
        val name = pair.substringBefore('=').trim()
        if (name.isEmpty()) return
        val value = pair.substringAfter('=', "").trim()
        if (value.isEmpty()) cookies.remove(name) else cookies[name] = value
    }
}
